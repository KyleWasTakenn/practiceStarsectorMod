package example.world.systems;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.Condition;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin.AsteroidFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;

import org.lazywizard.lazylib.MathUtils;

public class MySystem {

    // This method will generate
    public void generate(SectorAPI sector) {

        // Setting location of the system on the sector map
        StarSystemAPI system = sector.createStarSystem("My Star");
        system.getLocation().set(-39000, 39000); // top leftish

        // Creating the 'Star' / center of the system and adding a light color
        PlanetAPI petrichorBlackHole = system.initStar("Petrichor", "black_hole", 1100f, 450);
        system.setLightColor(new Color(142, 81, 223));

        system.setBackgroundTextureFilename("graphics/backgrounds/hyperspace1.jpg"); // base game background for testing

    }

}