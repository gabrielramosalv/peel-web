package net.peelweb.resp;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RequiredArgsConstructor
public class StaticContentAssembler implements ResponseAssembler {

    private final String staticContentPath;

    private Path getResourceFilePath(Path staticContentFolder, String resourcePath) {
        String referenceResourcePath = resourcePath;
        while (!referenceResourcePath.isEmpty()) {
            String electedResourcePath = resourcePath.substring(referenceResourcePath.lastIndexOf("/") + 1);

            Path resourceFilePath = staticContentFolder.resolve(electedResourcePath);
            if (Files.exists(resourceFilePath)) {
                return resourceFilePath;
            } else {
                referenceResourcePath = referenceResourcePath.substring(0,
                        resourcePath.lastIndexOf(electedResourcePath) - 1);
            }
        }
        return null;
    }

    @Override
    public String assembly(Object object) {
        Path staticContentFolder = Paths.get(this.staticContentPath);
        if (!Files.exists(staticContentFolder)) {
            throw new RuntimeException("Static content folder does not exist: " + staticContentFolder);
        }

        String resourcePath = (String) object;
        Path resourceFilePath = this.getResourceFilePath(staticContentFolder, resourcePath);
        if (resourceFilePath == null) {
            throw new RuntimeException("Resource file does not exist: " + resourcePath);
        }

        String content;
        try {
            content = String.join(" ", Files.readAllLines(resourceFilePath, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return content;
    }
}
