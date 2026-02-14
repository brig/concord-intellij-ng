package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public abstract class IdentityMetaType extends ConcordMetaType {

    private final String identity;

    protected IdentityMetaType(String identity) {
        this.identity = identity;
    }

    protected IdentityMetaType(String identity, @NotNull TypeProps props) {
        super(props);
        this.identity = identity;
    }

    public String getIdentity() {
        return identity;
    }

    @Override
    protected abstract @NotNull Map<String, YamlMetaType> getFeatures();

    @Override
    public @Nullable String getDocumentationExample() {
        var resourcePath = "/documentation/examples/" + identity + ".concord.yaml";
        try (var stream = IdentityMetaType.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                return null;
            }
            var content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            var lines = content.split("\n");
            if (lines.length <= 2) {
                return null;
            }
            var sb = new StringBuilder();
            for (int i = 2; i < lines.length; i++) {
                var line = lines[i];
                if (line.startsWith("    ")) {
                    line = line.substring(4);
                }
                if (i > 2) {
                    sb.append("\n");
                }
                sb.append(line);
            }
            return sb.toString().stripTrailing();
        } catch (IOException e) {
            return null;
        }
    }
}
