package com.tyv.storageservice.util;

import org.apache.commons.io.file.PathUtils;
import org.apache.http.entity.ContentType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.tyv.storageservice.enums.Bucket.PRODUCT;
import static com.tyv.storageservice.enums.Bucket.USER;

public class LocalStorageResourceUtil {


    public static FilePart createFilePart(ContentType contentType, FileConstants file) throws IOException, URISyntaxException {
        HttpHeaders headers = new HttpHeaders(MultiValueMap.fromMultiValue(
                Map.of(HttpHeaders.CONTENT_TYPE, List.of(contentType.toString()))));
        Path path = Path.of(Objects.requireNonNull(LocalStorageResourceUtil.class.getResource(FileConstants.SOURCE_DIR + file.getSourceName())).toURI());
        return new CustomFilePart(file.getSourceName(),
                headers,
                Files.readAllBytes(path));
    }

    public static void prepareTestDirectory(Path path) throws IOException {
        Path productPath = path.resolve(PRODUCT.toString().toLowerCase()).resolve(LocalDate.now().toString());
        Path userPath = path.resolve(USER.toString().toLowerCase()).resolve(LocalDate.now().toString());

        if (productPath.toFile().mkdirs()) {
            prepareTestFile(productPath, FileConstants.FROG);
            prepareTestFile(productPath, FileConstants.SNAKE);
            prepareTestFile(productPath, FileConstants.MESSAGE);
        }

        if (userPath.toFile().mkdirs()) {
            prepareTestFile(userPath, FileConstants.LIZARD);
            prepareTestFile(userPath, FileConstants.MESSAGE);
        }
    }

    private static void prepareTestFile(Path targetPath, FileConstants constant) throws IOException {
        PathUtils.copyFile(Objects.requireNonNull(
                LocalStorageResourceUtil.class.getResource(constant.getSourcePath())),
                targetPath.resolve(constant.getNameWithUUID()));
    }

    public static void deleteDirectory(Path path) throws IOException {
        if (PathUtils.isDirectory(path)) {
            PathUtils.deleteDirectory(path);
        }
    }

    public static boolean isEmptyDirectory(Path path) throws IOException {
        return !path.toFile().exists() || PathUtils.isEmptyDirectory(path);
    }

    public static boolean isDirectoryEmptyRecursively(Path root) throws IOException {
        if (root.toFile().exists()) {
            try (Stream<Path> ps = Files.walk(root)) {
                return ps.anyMatch(Files::isRegularFile);
            }
        }
        return false;
    }

    public static boolean isFilePresentRecursively(Path root, FileConstants constant) throws IOException {
        if (root.toFile().exists()) {
            try (Stream<Path> ps = Files.walk(root)) {
                return ps.filter(Files::isRegularFile)
                        .anyMatch(new Predicate<Path>() {
                            @Override
                            public boolean test(Path path) {
                                return path.toString().endsWith(constant.getNameWithUUID());
                            }
                        });
            }
        }
        return false;
    }
}
