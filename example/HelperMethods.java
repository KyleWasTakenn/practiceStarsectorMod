package example;

import com.fs.starfarer.api.Global;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.RingBandAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;

import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

import com.fs.starfarer.api.impl.campaign.procgen.StarGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;

import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;

import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.List;

public class HelperMethods {

    /* HELPER FUNCTIONS */

    /* DERRIVED FROM TAHLAN SHIPWORKS */
    /* Picked up from Varya tutorial. Used for creating marketplaces easily. */
    public static MarketAPI addMarketplace(
            String factionID,
            SectorEntityToken primaryEntity,
            List<SectorEntityToken> connectedEntities,
            String name,
            int popSize,
            List<String> marketConditions,
            List<String> submarkets,
            List<String> industries,
            float tariff,
            boolean isFreePort,
            boolean floatyJunk
    ) {
        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        String planetID = primaryEntity.getId();
        String marketID = planetID + "_market"; // IMPORTANT this is a naming convention for markets. didn't want to
        // have to pass in another variable :D

        MarketAPI newMarket = Global.getFactory().createMarket(marketID, name, popSize);
        newMarket.setFactionId(factionID);
        newMarket.setPrimaryEntity(primaryEntity);
        // newMarket.getTariff().modifyFlat("generator", tariff);
        newMarket.getTariff().setBaseValue(tariff);

        // Add submarkets, if any
        if (null != submarkets) {
            for (String market : submarkets) {
                newMarket.addSubmarket(market);
            }
        }

        // Add conditions
        for (String condition : marketConditions) {
            newMarket.addCondition(condition);
        }

        // Add industries
        for (String industry : industries) {
            newMarket.addIndustry(industry);
        }

        // Set free port
        newMarket.setFreePort(isFreePort);

        // Add connected entities, if any
        if (null != connectedEntities) {
            for (SectorEntityToken entity : connectedEntities) {
                newMarket.getConnectedEntities().add(entity);
            }
        }

        // set market in global, factions, and assign market, also submarkets
        globalEconomy.addMarket(newMarket, floatyJunk);
        primaryEntity.setMarket(newMarket);
        primaryEntity.setFaction(factionID);

        if (null != connectedEntities) {
            for (SectorEntityToken entity : connectedEntities) {
                entity.setMarket(newMarket);
                entity.setFaction(factionID);
            }
        }

        // Finally, return the newly-generated market
        return newMarket;

    }

    /* DERRIVED FROM TAHLAN SHIPWORKS */
    /* Helper method for creating an event horizon around black hole */
    /* Ended up making my own, but keeping this just in case */
    protected final void setBlackHole(
            PlanetAPI star,
            StarSystemAPI system
    ) {
        StarCoronaTerrainPlugin coronaPlugin = Misc.getCoronaFor(star);

        // If a corona is present on the star, remove it.
        if (coronaPlugin != null)
            system.removeEntity(coronaPlugin.getEntity());

        // Gets the stars data, stores is in a variable.
        StarGenDataSpec starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class,
                star.getSpec().getPlanetType(), false);

        // Calculates the corona size for reuse
        float corona = star.getRadius() * (starData.getCoronaMult()
                + starData.getCoronaVar() * (StarSystemGenerator.random.nextFloat() - 0.5F));

        // If corona size is below the minimum, sets equal to minimum.
        if (corona < starData.getCoronaMin())
            corona = starData.getCoronaMin();

        // Uses CoronaParams class to create an event horizon.
        system.addTerrain(
                "event_horizon",
                new StarCoronaTerrainPlugin.CoronaParams(
                        star.getRadius() + corona, (star.getRadius() + corona) / 2.0F,
                        (SectorEntityToken) star, starData.getSolarWind(),
                        starData.getMinFlare() + (starData.getMaxFlare() - starData.getMinFlare())
                                * StarSystemGenerator.random.nextFloat(),
                        starData.getCrLossMult()));

        // Creates an entity token for the event horizon using the corona params, and
        // places it in orbit around the star.
        SectorEntityToken eventHorizon = system.addTerrain("event_horizon", new StarCoronaTerrainPlugin.CoronaParams(
                star.getRadius() + corona, (star.getRadius() + corona) / 2.0F, (SectorEntityToken) star,
                starData.getSolarWind(),
                starData.getMinFlare()
                        + (starData.getMaxFlare() - starData.getMinFlare()) * StarSystemGenerator.random.nextFloat(),
                starData.getCrLossMult()));
        eventHorizon.setCircularOrbit((SectorEntityToken) star, 0.0F, 0.0F, 100.0F);
    }

    /* NON-DERRIVITIVE */
    /* Helper method for converting a star's corona to event horizon. */
    public static void convertToBlackhole(
            PlanetAPI star,
            StarSystemAPI system,
            Float ehRadius,
            Float ehPullRate, // Must be negative value! Positive pushes player away.
            Float crLossMult
    )
    {
        // Radius, PullRate, LossMult: can be null for defaults
        if (ehRadius == null) {
            ehRadius = 350f; // Vanilla's default
        }

        if (ehPullRate == null) {
            ehPullRate = -10f; // Vanilla's default
        }

        if (crLossMult == null) {
            crLossMult = 25f; // Vanilla's default
        }

        if (ehPullRate >= 0) {
            throw new IllegalArgumentException("Input must be a negative float, got: " + ehPullRate);
        }

        StarCoronaTerrainPlugin starCorona = Misc.getCoronaFor(star);
        if (starCorona != null)
            system.removeEntity(starCorona.getEntity());

        // starName, terrainType, terrainRadius, windBurnLevel, flareProbability, crLossMultiplier
        // 350f, -10f, 0f, 25f are the base-game default variables for an event horizon.
        system.addCorona(
                star,
                "event_horizon",
                ehRadius,
                ehPullRate,
                0f, crLossMult
        );
    }

    /* NON-DERRIVITIVE */
    /* Creating a 4-band accretion disk for black hole systems */
    public static void createAccretionDisks(
            PlanetAPI star,
            StarSystemAPI system,
            Float diskMinDist, // Distance of the closest of the rings. Each ring will be +400SU away.
            String texture1,
            String texture2,
            Float textureWidth,
            Float engineWidth,
            Color diskColor,
            Float orbitInterval // Set to negative to match spiral path.

    )
    {
        // defaults for texture1, texture2, textureWidth, engineWidth
        // Color can be null as well.
        if (texture1 == null) {
            texture1 = "rings_dust0";
        }

        if (texture2 == null) {
            texture2 = "rings_ice0";
        }

        if (textureWidth == null) {
            textureWidth = 512f;
        }

        if (engineWidth == null) {
            engineWidth = 400f;
        }

        // Creating black hole accretion disks
        // "rings_dust0", "rings_ice0" are default textures
        // focus, category, texture, textureWidth, index, color, engineWidth, distance, orbit days
        RingBandAPI accretionDisk1 = system.addRingBand(
                star, "misc", texture1,
                textureWidth, 0, null, engineWidth, diskMinDist, orbitInterval
        );
        accretionDisk1.setSpiral(true);
        accretionDisk1.setSpiralFactor(10f);
        accretionDisk1.setMinSpiralRadius(0f);

        RingBandAPI accretionDisk2 = system.addRingBand(
                star, "misc", texture2,
                textureWidth, 0, null, engineWidth, diskMinDist + 400f, orbitInterval
        );
        accretionDisk2.setSpiral(true);
        accretionDisk2.setSpiralFactor(7.5f);
        accretionDisk2.setMinSpiralRadius(0f);

        RingBandAPI accretionDisk3 = system.addRingBand(
                star, "misc", texture1,
                textureWidth, 0, null, engineWidth, diskMinDist + 800f, orbitInterval
        );
        accretionDisk3.setSpiral(true);
        accretionDisk3.setSpiralFactor(3f);
        accretionDisk3.setMinSpiralRadius(0f);

        RingBandAPI accretionDisk4 = system.addRingBand(
                star, "misc", texture2,
                textureWidth, 0, null, engineWidth, diskMinDist + 1200f, orbitInterval
        );
        accretionDisk4.setSpiral(true);
        accretionDisk4.setSpiralFactor(1f);
        accretionDisk4.setMinSpiralRadius(0f);

    }

}