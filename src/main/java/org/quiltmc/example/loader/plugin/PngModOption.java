package org.quiltmc.example.loader.plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.ExtendedFiles;
import org.quiltmc.loader.api.LoaderValue;
import org.quiltmc.loader.api.ModContributor;
import org.quiltmc.loader.api.ModDependency;
import org.quiltmc.loader.api.ModLicense;
import org.quiltmc.loader.api.QuiltFileSystems;
import org.quiltmc.loader.api.Version;
import org.quiltmc.loader.api.gui.QuiltLoaderGui;
import org.quiltmc.loader.api.gui.QuiltLoaderIcon;
import org.quiltmc.loader.api.gui.QuiltLoaderText;
import org.quiltmc.loader.api.plugin.ModContainerExt;
import org.quiltmc.loader.api.plugin.ModMetadataExt;
import org.quiltmc.loader.api.plugin.QuiltPluginContext;
import org.quiltmc.loader.api.plugin.solver.ModLoadOption;
import org.quiltmc.loader.api.plugin.solver.QuiltFileHasher;

import net.fabricmc.loader.api.metadata.ModEnvironment;

import net.fabricmc.api.EnvType;

public class PngModOption extends ModLoadOption {

    public final ExampleLoaderPlugin plugin;
    public final Path file;
    private final Path resourceRoot;

    private final QuiltLoaderIcon icon;
    private final ModMetadataExt metadata;

    public PngModOption(ExampleLoaderPlugin plugin, Path file) throws IOException {
        this.plugin = plugin;
        this.file = file;
        String fileName = file.getFileName().toString();
        this.resourceRoot = QuiltFileSystems.createExtendedFileSystem("png-mod-" + fileName).root;
        ExtendedFiles.mount(file, resourceRoot.resolve(fileName));
        String modName = fileName.substring(0, fileName.length() - ".png".length());

        this.icon = QuiltLoaderGui.createIcon(Files.readAllBytes(file));

        this.metadata = new ModMetadataExt() {
            @Override
            public Collection<String> mixins(EnvType env) {
                return Collections.emptyList();
            }

            @Override
            public ModEnvironment environment() {
                return ModEnvironment.UNIVERSAL;
            }

            @Override
            public Collection<String> accessWideners() {
                return Collections.emptyList();
            }

            @Override
            public Version version() {
                return Version.of("1.0");
            }

            @Override
            public Map<String, LoaderValue> values() {
                return Collections.emptyMap();
            }

            @Override
            public @Nullable LoaderValue value(String key) {
                return null;
            }

            @Override
            public String name() {
                return modName;
            }

            @Override
            public Collection<ModLicense> licenses() {
                return Collections.emptyList();
            }

            @Override
            public String id() {
                return modName;
            }

            @Override
            public @Nullable String icon(int size) {
                return fileName;
            }

            @Override
            public String group() {
                return "org.quiltmc.example";
            }

            @Override
            public @Nullable String getContactInfo(String key) {
                return null;
            }

            @Override
            public String description() {
                return "An Image!";
            }

            @Override
            public Collection<ModDependency> depends() {
                return Collections.emptyList();
            }

            @Override
            public Collection<ModContributor> contributors() {
                return Collections.emptyList();
            }

            @Override
            public boolean containsValue(String key) {
                return false;
            }

            @Override
            public Map<String, String> contactInfo() {
                return Collections.emptyMap();
            }

            @Override
            public Collection<ModDependency> breaks() {
                return Collections.emptyList();
            }

            @Override
            public @Nullable ModPlugin plugin() {
                return null;
            }

            @Override
            public Map<String, String> languageAdapters() {
                return Collections.emptyMap();
            }

            @Override
            public Map<String, Collection<ModEntrypoint>> getEntrypoints() {
                return Collections.emptyMap();
            }
        };
    }

    @Override
    public QuiltPluginContext loader() {
        return plugin.getContext();
    }

    @Override
    public ModMetadataExt metadata() {
        return metadata;
    }

    @Override
    public Path from() {
        return file;
    }

    @Override
    public Path resourceRoot() {
        return resourceRoot;
    }

    @Override
    public boolean isMandatory() {
        return true;
    }

    @Override
    public @Nullable String namespaceMappingFrom() {
        return null;
    }

    @Override
    public boolean needsTransforming() {
        return false;
    }

    @Override
    public byte[] computeOriginHash(QuiltFileHasher hasher) throws IOException {
        return hasher.computeNormalHash(file);
    }

    @Override
    public QuiltLoaderIcon modFileIcon() {
        return icon;
    }

    @Override
    public QuiltLoaderIcon modTypeIcon() {
        return QuiltLoaderGui.iconUnknownFile();
    }

    @Override
    public ModContainerExt convertToMod(Path transformedResourceRoot) {
        List<List<Path>> sourcePaths = plugin.getContext().manager().convertToSourcePaths(file);
        return new PngModContainer(plugin, transformedResourceRoot, sourcePaths, metadata());
    }

    @Deprecated
    @Override
    public String shortString() {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Deprecated
    @Override
    public String getSpecificInfo() {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public QuiltLoaderText describe() {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }
}
