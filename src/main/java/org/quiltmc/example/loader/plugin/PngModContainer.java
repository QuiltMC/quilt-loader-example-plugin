package org.quiltmc.example.loader.plugin;

import java.nio.file.Path;
import java.util.List;

import org.quiltmc.loader.api.plugin.ModContainerExt;
import org.quiltmc.loader.api.plugin.ModMetadataExt;

public class PngModContainer implements ModContainerExt {

    public final String pluginId;
    public final Path rootPath;
    public final List<List<Path>> sourcePaths;
    public final ModMetadataExt metadata;

    PngModContainer(ExampleLoaderPlugin plugin, Path rootPath, List<List<Path>> sourcePaths, ModMetadataExt metadata) {
        this.pluginId = plugin.getContext().pluginId();
        this.rootPath = rootPath;
        this.sourcePaths = sourcePaths;
        this.metadata = metadata;
    }

    @Override
    public Path rootPath() {
        return rootPath;
    }

    @Override
    public List<List<Path>> getSourcePaths() {
        return sourcePaths;
    }

    @Override
    public BasicSourceType getSourceType() {
        return BasicSourceType.OTHER;
    }

    @Override
    public ModMetadataExt metadata() {
        return metadata;
    }

    @Override
    public String pluginId() {
        return pluginId;
    }

    @Override
    public String modType() {
        return "PNG Mod";
    }

    @Override
    public boolean shouldAddToQuiltClasspath() {
        return true;
    }
}
