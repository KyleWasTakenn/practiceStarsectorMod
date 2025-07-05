package example;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

// Plugin class used to initialize the mod into the game on save creation (new game)
public class MyModPlugin extends BaseModPlugin {

    //
    private static void initMyMod() {
        new MyModGen().generate(Global.getSector());
    }

    @Override
    public void onNewGame() {
        Global.getLogger(this.getClass()).info("Log test successful! Mod plugin successfully loaded.");

        initMyMod();
    }

}