package example.world.systems;
import example.HelperMethods;

import java.awt.Color;
import java.util.Arrays;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.terrain.*;
import com.fs.starfarer.api.impl.campaign.procgen.*;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

import com.fs.starfarer.api.util.Misc;

import org.lazywizard.lazylib.MathUtils;

public class MySystem {
    // Distance variables (For easier adjustment and tweaks)
    final float commDist = 500f;
    final float jumpDist = 9000f;

    final float stableLocation1Dist = 3000f;
    final float stableLocation2Dist = 2500f;

    final float petrichorStationDist = 750f;

//    final float accretionDisk1Dist = 1250f;
//    final float accretionDiskMinDist = 0f;
//    final float accretionDisk2Dist = accretionDisk1Dist - 450;

    /* GENERATE METHOD */
    /* Invoke to generate the system */
    public void generate(SectorAPI sector) {

        StarSystemAPI system = sector.createStarSystem("Petrichor");
        system.getLocation().set(21000, -10000); // near Diable Avionics for testing

        // Star creation
        PlanetAPI petrichorBlackHole = system.initStar(
                "petrichor_bh",
                "black_hole",
                150f,
                0f);
        system.setLightColor(new Color(142, 81, 223));

        // Using my helper methods to fully convert star to a blackhole.
        HelperMethods.convertToBlackhole(
                petrichorBlackHole,
                system,
                null,
                null,
                null
        );
        HelperMethods.createAccretionDisks(
                petrichorBlackHole,
                system,
                800f,
                null,
                null,
                800f,
                800f,
                null,
                -180f
        );

        SectorEntityToken outterAsteroidBelt = system.addAsteroidBelt(
                petrichorBlackHole,
                500, // numAsteroids
                2000f, // orbitRadius
                200f, // width
                -100f, // minOrbitDays
                -200f, // maxOrbitDays
                Terrain.ASTEROID_BELT,
                "Asteroid Belt"
        );

        // Station creation
        SectorEntityToken petrichorStation = system.addCustomEntity(
                "petrichor_station", // id
                "Petrichor Station", // name
                "mymod_petrichor_station", // custom_entity.json id
                "veil_syndicate" // faction
        );
        petrichorStation.setCircularOrbitPointingDown(
                petrichorBlackHole,
                0,
                petrichorStationDist,
                220
        );
        petrichorStation.setCustomDescriptionId("desc_petrichor_station");

        // Other entities creation (comm, stable points, gate)
        SectorEntityToken petrichorComm = system.addCustomEntity(
                "petrichor_comm_relay",
                "Petrichor Comm Relay",
                "comm_relay",
                Factions.NEUTRAL
        );
        SectorEntityToken petrichorStableLocation1 = system.addCustomEntity(
                "petrichor_stable_location2",
                "Stable Location",
                "stable_location",
                Factions.NEUTRAL
        );
        SectorEntityToken petrichorStableLocation2 = system.addCustomEntity(
                "petrichor_stable_location3",
                "Stable Location",
                "stable_location",
                Factions.NEUTRAL
        );
        SectorEntityToken petrichorJump = system.addCustomEntity(
                "petrichor_gate",
                "Inactive Gate",
                "inactive_gate",
                Factions.NEUTRAL
        );

        petrichorComm.setCircularOrbit(
                petrichorStation, // focus (point of orbit)
                MathUtils.getRandomNumberInRange(0f, 360f), // Randomized starting angle
                commDist, // Distance from focus
                520f // orbitDays
        );
        petrichorStableLocation1.setCircularOrbit(
                petrichorBlackHole,
                MathUtils.getRandomNumberInRange(0f, 360f),
                stableLocation1Dist,
                520f
        );
        petrichorStableLocation2.setCircularOrbit(
                petrichorBlackHole,
                MathUtils.getRandomNumberInRange(0f, 360f),
                stableLocation2Dist,
                520f
        );
        petrichorJump.setCircularOrbit(
                petrichorBlackHole,
                MathUtils.getRandomNumberInRange(0f, 360f),
                jumpDist,
                520f
        );

        // Creating market
        MarketAPI petrichorMarket = HelperMethods.addMarketplace(
                "veil_syndicate",
                petrichorStation,
                null,
                "Petrichor Station",
                7,
                Arrays.asList(
                        Conditions.POPULATION_7,
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
        petrichorMarket.getIndustry
                (Industries.HIGHCOMMAND).setAICoreId(Commodities.ALPHA_CORE);
        petrichorMarket.getIndustry
                (Industries.STARFORTRESS_HIGH).setAICoreId(Commodities.ALPHA_CORE);
        petrichorMarket.getIndustry
                (Industries.ORBITALWORKS).setAICoreId(Commodities.ALPHA_CORE);
        petrichorMarket.getIndustry
                (Industries.POPULATION).setAICoreId(Commodities.ALPHA_CORE);
        petrichorMarket.getIndustry
                (Industries.FUELPROD).setAICoreId(Commodities.ALPHA_CORE);
        petrichorMarket.getIndustry
                (Industries.MEGAPORT).setAICoreId(Commodities.ALPHA_CORE);
        petrichorMarket.getIndustry(
                Industries.WAYSTATION).setAICoreId(Commodities.ALPHA_CORE);
        petrichorMarket.getIndustry
                (Industries.HEAVYBATTERIES).setAICoreId(Commodities.ALPHA_CORE);
        petrichorMarket.getIndustry
                (Industries.PLANETARYSHIELD).setAICoreId(Commodities.ALPHA_CORE);
        petrichorMarket.getIndustry
                (Industries.CRYOSANCTUM).setAICoreId(Commodities.ALPHA_CORE);

        // Inserting Industry items into industries
        petrichorMarket.getIndustry
                (Industries.ORBITALWORKS).setSpecialItem
                (
                        new SpecialItemData("pristine_nanoforge", null)
                );
        petrichorMarket.getIndustry
                (Industries.HEAVYBATTERIES).setSpecialItem
                (
                        new SpecialItemData("drone_replicator", null)
                );
        petrichorMarket.getIndustry
                (Industries.HIGHCOMMAND).setSpecialItem
                (
                        new SpecialItemData
                                ("cryoarithmetic_engine", null)
                );
        petrichorMarket.getIndustry
                (Industries.FUELPROD).setSpecialItem
                (
                        new SpecialItemData
                                ("synchrotron", null)
                );
        petrichorMarket.getIndustry
                (Industries.MEGAPORT).setSpecialItem
                (
                        new SpecialItemData
                                ("fullerene_spool", null)
                );

        // Manual jumppoint creation
        // JumpPointAPI jumpPointFringe =
        // Global.getFactory().createJumpPoint("fringe_jump", "Fringe System Jump");
        // jumpPointFringe.setCircularOrbit(system.getEntityById("petrichor_bh"), 2,
        // jumpFringeDist, 4000f);
        // jumpPointFringe.setStandardWormholeToHyperspaceVisual();
        // system.addEntity(jumpPointFringe);

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

        // helper logic from Varya for creating automatic jump points.
        system.autogenerateHyperspaceJumpPoints(true, true);

        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;

        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(
                system.getLocation().x,
                system.getLocation().y,
                0,
                radius + minRadius,
                0,
                360f
        );

        editor.clearArc(
                system.getLocation().x,
                system.getLocation().y,
                0,
                radius + minRadius,
                0,
                360f,
                0.25f
        );

        // Background image for the system
        system.setBackgroundTextureFilename("graphics/backgrounds/hyperspace1.jpg");
    }

}