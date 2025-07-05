package example.world;

import com.fs.starfarer.api.campaign.SectorAPI;
import example.world.systems.MySystem; // Importing our system and adding it to our mods generation

public class MyModGen {

    public void generate(SectorAPI sector) {
        // This is where to add a system to be generated. Each system needs its own file
        // in worlds.systems
        new MySystem().generate(sector);
    }

}
