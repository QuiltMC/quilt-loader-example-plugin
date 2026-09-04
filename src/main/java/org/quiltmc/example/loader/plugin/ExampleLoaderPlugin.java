package org.quiltmc.example.loader.plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.quiltmc.loader.api.LoaderValue;
import org.quiltmc.loader.api.gui.QuiltTreeNode;
import org.quiltmc.loader.api.plugin.ModLocation;
import org.quiltmc.loader.api.plugin.QuiltLoaderPlugin;
import org.quiltmc.loader.api.plugin.QuiltPluginContext;
import org.quiltmc.loader.api.plugin.solver.ModLoadOption;

public class ExampleLoaderPlugin implements QuiltLoaderPlugin {

    private QuiltPluginContext context;

    public QuiltPluginContext getContext() {
        if (context == null) {
            throw new IllegalStateException("Not yet loaded!");
        }
        return context;
    }

    @Override
    public void load(QuiltPluginContext ctx, Map<String, LoaderValue> previousData) {
        this.context = ctx;
    }

    @Override
    public void unload(Map<String, LoaderValue> data) {
        // We don't store any state, so we don't need to load/unload
    }

    @Override
    public ModLoadOption[] scanUnknownFile(Path file, ModLocation location, QuiltTreeNode guiNode) throws IOException {

        context.logger().info("FILE: " + file);

        String fileName = file.getFileName().toString();

        if (fileName.endsWith(".png")) {

            // This plugin loads *.png files as mods
            // These mods don't do anything special, other than use that png file as their icon

            return new ModLoadOption[] { new PngModOption(this, file) };
        } else if (fileName.endsWith(".json")) {

            // Test ExtendedFiles.mountSubFile
            // by mounting each json element in its own file
            // pointing to the sub-part of the real json file

            return new ModLoadOption[] { new PackedJsonModOption(this, file) };
        } else {
            return null;
        }
    }
}
