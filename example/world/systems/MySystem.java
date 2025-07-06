package example.world.systems;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;

import org.lazywizard.lazylib.MathUtils;

public class MySystem {

    /**
     * Shorthand function for adding a market -- this is derived from tahlan mod
     */
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

    // Distance variables (For easier adjustment and tweaks)
    final float stableLocation1Dist = 4000f;
    final float petrichorStationDist = 3000f;

    final float jumpFringeDist = 8000;

    // This method will generate the star, planets, and other objects in the sector
    // when invoked
    public void generate(SectorAPI sector) {

        // Setting location of the system on the sector map
        StarSystemAPI system = sector.createStarSystem("Petrichor");
        system.getLocation().set(21000, -10000); // near Diable Avionics for testing

        // Creating the 'Star' / center of the system and adding a light color
        PlanetAPI petrichorBlackHole = system.initStar("petrichor_bh", "black_hole", 150f, 450);
        system.setLightColor(new Color(142, 81, 223));

        // Creating stable point entities, and setting their location.

        SectorEntityToken petrichorStableLocation1 = system.addCustomEntity("petrichor_stableLocation1",
                "Stable Location", "stable_location", Factions.NEUTRAL);
        // setCircularOrbit needs an object of type SectorEntityToken, a starting angle
        // (float), a distance from the centre (float), and orbit days
        petrichorStableLocation1.setCircularOrbit(petrichorBlackHole, MathUtils.getRandomNumberInRange(0f, 360f),
                stableLocation1Dist, 520);

        SectorEntityToken petrichorStation = system.addCustomEntity("petrichor_station", "Petrichor Station",
                "mymod_petrichor_station", "pirates");
        petrichorStation.setCircularOrbitPointingDown(petrichorBlackHole, 0, petrichorStationDist, 220);
        petrichorStation.setCustomDescriptionId("test_petrichor_station");

        // Creating the market for a planet / station:

        // factionID, PlanetAPI, Industries (can be null), display name, market size
        // (int), market conditions (can be null), submarkets (black market etc)
        MarketAPI petrichorMarket = addMarketplace("pirates", petrichorStation, null, "Petrichor Station", 4,
                Arrays.asList(
                        Conditions.POPULATION_4,
                        Conditions.NO_ATMOSPHERE,
                        Conditions.OUTPOST,
                        Conditions.AI_CORE_ADMIN),
                Arrays.asList(
                        Submarkets.GENERIC_MILITARY,
                        Submarkets.SUBMARKET_STORAGE,
                        Submarkets.SUBMARKET_BLACK),
                Arrays.asList(
                        Industries.POPULATION,
                        Industries.SPACEPORT,
                        Industries.BATTLESTATION_HIGH,
                        Industries.HEAVYBATTERIES,
                        Industries.MILITARYBASE,
                        Industries.ORBITALWORKS,
                        Industries.WAYSTATION),
                0.05f,
                false,
                false);

        // Inserting AI cores into industries

        petrichorMarket.getIndustry(Industries.MILITARYBASE).setAICoreId(Commodities.ALPHA_CORE);
        petrichorMarket.getIndustry(Industries.BATTLESTATION_HIGH).setAICoreId(
                Commodities.ALPHA_CORE);
        petrichorMarket.getIndustry(Industries.ORBITALWORKS).setAICoreId(Commodities.ALPHA_CORE);

        // Manual jumppoint while there are no valid markers to auto generate any
        JumpPointAPI jumpPointFringe = Global.getFactory().createJumpPoint("fringe_jump", "Fringe System Jump");
        jumpPointFringe.setCircularOrbit(system.getEntityById("petrichor_bh"), 2,
                jumpFringeDist, 4000f);
        jumpPointFringe.setStandardWormholeToHyperspaceVisual();

        // helper logic from Varya for creating automatic jump points.
        system.autogenerateHyperspaceJumpPoints(true, false);

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