package example.world.systems;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import com.fs.starfarer.api.Global;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.econ.impl.PlanetaryShield;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.terrain.*;
import com.fs.starfarer.api.impl.campaign.procgen.*;

import com.fs.starfarer.api.campaign.PlanetSpecAPI;

import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

// import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
// import com.fs.starfarer.api.impl.campaign.procgen.StarGenDataSpec;
// import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
// import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
// import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.util.Misc;

import org.lazywizard.lazylib.MathUtils;

public class MySystem {

    /* HELPER FUNCTIONS */

    /* DERRIVED FROM TAHLAN SHIPWORKS */
    /* Picked up from Varya tutorial. Used for creating marketplaces easily. */
    public static MarketAPI addMarketplace(String factionID, SectorEntityToken primaryEntity,
            List<SectorEntityToken> connectedEntities, String name,
            int popSize, List<String> marketConditions, List<String> submarkets, List<String> industries, float tariff,
            boolean isFreePort, boolean floatyJunk) {
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
    /* Comments by me so I can remember wtf is going on later */
    protected final void setBlackHole(PlanetAPI star, StarSystemAPI system) {
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

    // Distance variables (For easier adjustment and tweaks)
    final float stableLocation1Dist = 3900f;
    final float stableLocation2Dist = 2800f;
    final float stableLocation3Dist = 1700f;

    final float petrichorStationDist = 750f;

    // final float jumpFringeDist = 8000f;

    final float accretionDisk1Dist = 1250f;
    final float accretionDiskMinDist = 0f;
    final float accretionDisk2Dist = accretionDisk1Dist - 450;

    /* GENERATE METHOD */
    /* Invoke to generate the system */
    public void generate(SectorAPI sector) {

        // Setting location of the system on the sector map
        StarSystemAPI system = sector.createStarSystem("Petrichor");
        system.getLocation().set(21000, -10000); // near Diable Avionics for testing

        // Creating the 'Star' / center of the system and adding a light color
        PlanetAPI petrichorBlackHole = system.initStar(
                "petrichor_bh",
                "black_hole",
                150f,
                0f);
        system.setLightColor(new Color(142, 81, 223));

        // Removing the corona entity completely
        StarCoronaTerrainPlugin starCorona = Misc.getCoronaFor(petrichorBlackHole);
        if (starCorona != null)
            system.removeEntity(starCorona.getEntity());

        // starName, terrainType, terrainRadius, windBurnLevel
        // flareProbability, crLossMult
        system.addCorona(petrichorBlackHole, "event_horizon", 350f, -10, 0, 25);

        // Creating black hole accretion disk
        RingBandAPI petrichorAccretionDisk1 = system.addRingBand(
                petrichorBlackHole, "misc", "rings_dust0",
                512f, 0, null, 400f, accretionDisk1Dist, -150f
        );
        petrichorAccretionDisk1.setSpiral(true);
        petrichorAccretionDisk1.setSpiralFactor(0.75f);
        petrichorAccretionDisk1.setMinSpiralRadius(accretionDiskMinDist);

        RingBandAPI petrichorAccretionDisk2 = system.addRingBand(
                petrichorBlackHole, "misc", "rings_ice0",
                512f, 0, null, 400f, accretionDisk2Dist, -180f
        );
        petrichorAccretionDisk2.setSpiral(true);
        petrichorAccretionDisk2.setSpiralFactor(1f);
        petrichorAccretionDisk2.setMinSpiralRadius(accretionDiskMinDist);

//        RingBandAPI petrichorAccretionDisk3 = system.addRingBand(
//                petrichorBlackHole, "misc", "rings_dust0",
//                1024f, 0, null, 450f, accretionDiskDist - 75, 252f
//        );
//        petrichorAccretionDisk3.setSpiral(true);
//        petrichorAccretionDisk3.setSpiralFactor(1.1f);

        // Creating stable point entities, and setting their location.
        SectorEntityToken petrichorStableLocation1 = system.addCustomEntity(
                "petrichor_stableLocation1",
                "Stable Location",
                "stable_location",
                Factions.NEUTRAL);
        SectorEntityToken petrichorStableLocation2 = system.addCustomEntity(
                "petrichor_stableLocation2",
                "Stable Location",
                "stable_location",
                Factions.NEUTRAL);
        SectorEntityToken petrichorStableLocation3 = system.addCustomEntity(
                "petrichor_stableLocation3",
                "Stable Location",
                "stable_location",
                Factions.NEUTRAL);
        // setCircularOrbit needs an object of type SectorEntityToken, a starting angle
        // (float), a distance from the centre (float), and orbit days
        petrichorStableLocation1.setCircularOrbit(
                petrichorBlackHole,
                MathUtils.getRandomNumberInRange(0f, 360f),
                stableLocation1Dist,
                520);
        petrichorStableLocation2.setCircularOrbit(
                petrichorBlackHole,
                MathUtils.getRandomNumberInRange(0f, 360f),
                stableLocation2Dist,
                520);
        petrichorStableLocation3.setCircularOrbit(petrichorBlackHole, MathUtils.getRandomNumberInRange(0f, 360f),
                stableLocation3Dist, 520);

        SectorEntityToken petrichorStation = system.addCustomEntity(
                "petrichor_station",
                "Petrichor Station",
                "mymod_petrichor_station",
                "pirates");
        petrichorStation.setCircularOrbitPointingDown(petrichorBlackHole, 0, petrichorStationDist, 220);
        petrichorStation.setCustomDescriptionId("test_petrichor_station");

        /* Code for custom planetary shield around Petrichor Station */
//        SectorEntityToken petrichorStationShield = system.addCustomEntity(
//                "petrichor_station_shield",
//                "Petrichor Station Shield",
//                "mymod_petrichor_station_shield",
//                "pirates"
//        );
//
//        petrichorStationShield.setCircularOrbit(
//                petrichorStation,
//                0,
//                0,
//                0
//        );
//
//        petrichorStationShield.addTag(Tags.NON_CLICKABLE);

        // Creating the market for a planet / station:
        // factionID, PlanetAPI, Industries (can be null), display name, market size
        // (int), market conditions (can be null), submarkets (black market etc.)
        MarketAPI petrichorMarket = addMarketplace(
                "pirates",
                petrichorStation,
                null,
                "Petrichor Station",
                7,
                Arrays.asList(
                        Conditions.POPULATION_4,
                        Conditions.NO_ATMOSPHERE,
                        Conditions.OUTPOST,
                        Conditions.AI_CORE_ADMIN,
                        Conditions.VERY_HOT,
                        Conditions.ORGANIZED_CRIME,
                        Conditions.PIRACY_RESPITE,
                        Conditions.STEALTH_MINEFIELDS,
                        Conditions.FRONTIER),
                Arrays.asList(
                        Submarkets.SUBMARKET_OPEN,
                        Submarkets.GENERIC_MILITARY,
                        Submarkets.SUBMARKET_STORAGE,
                        Submarkets.SUBMARKET_BLACK),
                Arrays.asList(
                        Industries.POPULATION,
                        Industries.MEGAPORT,
                        Industries.ORBITALWORKS,
                        Industries.WAYSTATION,
                        Industries.STARFORTRESS_HIGH,
                        Industries.HEAVYBATTERIES,
                        Industries.HIGHCOMMAND,
                        Industries.CRYOSANCTUM,
                        Industries.FUELPROD,
                        Industries.PLANETARYSHIELD),
                0.05f,
                false,
                true
        );

        // Inserting AI cores into industries
        petrichorMarket.getIndustry(
                Industries.HIGHCOMMAND).setAICoreId(Commodities.ALPHA_CORE);
        petrichorMarket.getIndustry(
                Industries.STARFORTRESS_HIGH).setAICoreId(Commodities.ALPHA_CORE);
        petrichorMarket.getIndustry(
                Industries.ORBITALWORKS).setAICoreId(Commodities.ALPHA_CORE);
        petrichorMarket.getIndustry(
                Industries.POPULATION).setAICoreId(Commodities.ALPHA_CORE);
        petrichorMarket.getIndustry(
                Industries.FUELPROD).setAICoreId(Commodities.ALPHA_CORE);
        petrichorMarket.getIndustry(
                Industries.MEGAPORT).setAICoreId(Commodities.ALPHA_CORE);
        petrichorMarket.getIndustry(
                Industries.WAYSTATION).setAICoreId(Commodities.ALPHA_CORE);
        petrichorMarket.getIndustry(
                Industries.HEAVYBATTERIES).setAICoreId(Commodities.ALPHA_CORE);
        petrichorMarket.getIndustry(
                Industries.PLANETARYSHIELD).setAICoreId(Commodities.ALPHA_CORE);
        petrichorMarket.getIndustry(
                Industries.CRYOSANCTUM).setAICoreId(Commodities.ALPHA_CORE);

        // Inserting Industry items into industries
        petrichorMarket.getIndustry(
                Industries.ORBITALWORKS).setSpecialItem(new SpecialItemData("pristine_nanoforge", null));
        petrichorMarket.getIndustry(
                Industries.HEAVYBATTERIES).setSpecialItem(new SpecialItemData("drone_replicator", null));
        petrichorMarket.getIndustry(
                Industries.HIGHCOMMAND).setSpecialItem(new SpecialItemData("cryoarithmetic_engine", null));
        petrichorMarket.getIndustry(
                Industries.FUELPROD).setSpecialItem(new SpecialItemData("synchrotron", null));
        petrichorMarket.getIndustry(
                Industries.MEGAPORT).setSpecialItem(new SpecialItemData("fullerene_spool", null));

        // Manual jumppoint creation
        // JumpPointAPI jumpPointFringe =
        // Global.getFactory().createJumpPoint("fringe_jump", "Fringe System Jump");
        // jumpPointFringe.setCircularOrbit(system.getEntityById("petrichor_bh"), 2,
        // jumpFringeDist, 4000f);
        // jumpPointFringe.setStandardWormholeToHyperspaceVisual();
        // system.addEntity(jumpPointFringe);

        // helper logic from Varya for creating automatic jump points.
        system.autogenerateHyperspaceJumpPoints(true, true);

        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;

        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);

        // Background image for the system
        system.setBackgroundTextureFilename("graphics/backgrounds/hyperspace1.jpg"); // base game background
                                                                                     // for testing
    }

}