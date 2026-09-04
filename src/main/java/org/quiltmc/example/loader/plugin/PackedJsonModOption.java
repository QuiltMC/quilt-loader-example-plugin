package org.quiltmc.example.loader.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.example.loader.plugin.lib.JsonFormat;
import org.quiltmc.example.loader.plugin.lib.JsonReader;
import org.quiltmc.example.loader.plugin.lib.JsonToken;
import org.quiltmc.loader.api.ExtendedFileSystem;
import org.quiltmc.loader.api.ExtendedFiles;
import org.quiltmc.loader.api.LoaderValue;
import org.quiltmc.loader.api.ModContributor;
import org.quiltmc.loader.api.ModDependency;
import org.quiltmc.loader.api.ModLicense;
import org.quiltmc.loader.api.MountOption;
import org.quiltmc.loader.api.QuiltFileSystems;
import org.quiltmc.loader.api.QuiltFileSystems.ExtendedFileSystemRef;
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

public class PackedJsonModOption extends ModLoadOption {

    public final ExampleLoaderPlugin plugin;
    public final Path file;
    private final Path resourceRoot;

    private final ModMetadataExt metadata;

    public PackedJsonModOption(ExampleLoaderPlugin plugin, Path file) throws IOException {
        this.plugin = plugin;
        this.file = file;
        String fileName = file.getFileName().toString();
        ExtendedFileSystemRef fsRef = QuiltFileSystems.createExtendedFileSystem("json-mod-" + fileName);
        this.resourceRoot = fsRef.root;
        ExtendedFileSystem ext = fsRef.quiltFileSystem;
        String modName = fileName.substring(0, fileName.length() - ".json".length());

        ext.mount(file, resourceRoot.resolve("json"), MountOption.READ_ONLY);

        class StackElement {
            final String path;
            final JsonToken token;
            final int start;

            StackElement(String path, JsonToken token, int start) {
                this.path = path;
                this.token = token;
                this.start = start;
            }

            @Override
            public String toString() {
                return token.toString();
            }
        }

        // Attempts to mount elements sub-elements in their own files
        // This doesn't actually work (this seems to be offset+1 from start)
        // but that's unimportant, since this only exists to test mountSubFile

        try (JsonReader reader = JsonReader.create(file, JsonFormat.JSON)) {

            Deque<StackElement> stack = new ArrayDeque<>();

            JsonToken next;

            loop: while (true) {
                next = reader.peek();
                int position = reader.getPos();

                switch (next) {
                    case BEGIN_OBJECT, BEGIN_ARRAY -> {
                        if (next == JsonToken.BEGIN_ARRAY) {
                            reader.beginArray();
                        } else {
                            reader.beginObject();
                        }
                        stack.push(new StackElement(reader.path(), next, position));
                        continue;
                    }
                    case END_DOCUMENT -> {
                        if (!stack.isEmpty()) {
                            throw new IOException("Encounted end-of-ducment but we haven't closed " + stack);
                        }
                        break loop;
                    }
                    case NAME -> {
                        // Names are part of objects?
                        // since it's not a value we don't store it in a file
                        reader.nextName();
                        continue;
                    }
                    case END_OBJECT, END_ARRAY -> {
                        JsonToken begin = next == JsonToken.END_ARRAY ? JsonToken.BEGIN_ARRAY : JsonToken.BEGIN_OBJECT;
                        StackElement element = stack.poll();
                        if (element == null || element.token != begin) {
                            throw new IOException("Mismatched " + next + " with start: " + element);
                        }
                        if (next == JsonToken.END_ARRAY) {
                            reader.endArray();
                        } else {
                            reader.endObject();
                        }
                        String path = element.path;
                        int start = element.start;
                        int end = reader.getPos();
                        mountSubFile(file, ext, path, start, end);
                    }
                    default -> {
                        int start = reader.getPos();
                        switch (next) {
                            case BEGIN_ARRAY, BEGIN_OBJECT, END_ARRAY, END_OBJECT, END_DOCUMENT, NAME -> {
                                throw new IllegalStateException("We already tested for " + next + "?!");
                            }
                            case BOOLEAN -> reader.nextBoolean();
                            case NULL -> reader.nextNull();
                            case NUMBER -> reader.nextNumber();
                            case STRING -> reader.nextString();
                        }
                        int end = reader.getPos();
                        String path = reader.getPreviousPath();
                        mountSubFile(file, ext, path, start, end);
                    }
                }

            }
        }

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
                return null;
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
                return "A Json File!";
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

    private void mountSubFile(Path source, ExtendedFileSystem ext, String path, int start, int end) throws IOException {
        loader().logger().info("Mounting " + path + " @" + start + " +" + (end - start));
        ext.mountSubFile(source, start, end - start, null, end-start, resourceRoot.resolve(path), MountOption.READ_ONLY, MountOption.REPLACE_EXISTING);
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
        return QuiltLoaderGui.iconJsonFile();
    }

    @Override
    public QuiltLoaderIcon modTypeIcon() {
        return QuiltLoaderGui.iconUnknownFile();
    }

    @Override
    public ModContainerExt convertToMod(Path transformedResourceRoot) {
        List<List<Path>> sourcePaths = plugin.getContext().manager().convertToSourcePaths(file);
        return new PackedJsonModContainer(plugin, transformedResourceRoot, sourcePaths, metadata());
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
