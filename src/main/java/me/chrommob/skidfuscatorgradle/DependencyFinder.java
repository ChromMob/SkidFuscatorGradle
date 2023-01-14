package me.chrommob.skidfuscatorgradle;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class DependencyFinder {
    private final File mavenDirectory;
    private final File skidDirectory;
    public DependencyFinder(File mavenDirectory, File skidDirectory) {
        this.mavenDirectory = mavenDirectory;
        this.skidDirectory = skidDirectory;
    }
    private Set<String> foundDependencies = new HashSet<>();

    public void addDependency(String dependency) {
        foundDependencies.add(dependency);
    }

    public boolean isFound(String dependency) {
        return foundDependencies.contains(dependency);
    }

    public DependencyResponse getDependency(Dependency dependency) {
        File file = getFromLocalRepository(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
        if (file != null) {
            Set<Dependency> subDependencies = getSubDependencies(file);
            return new DependencyResponse(file, subDependencies);
        }
        for (String repository : dependency.getRepositories()) {
            file = getFromRepository(repository, dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
            if (file != null) {
                Set<Dependency> subDependencies = getSubDependencies(file);
                return new DependencyResponse(file, subDependencies);
            }
        }
        System.out.println("Could not find dependency " + dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion());
        return null;
    }

    private Set<Dependency> getSubDependencies(File file) {
        File parent = file.getParentFile();
        File pom = new File(parent, file.getName().replace(".jar", ".pom"));
        if (!pom.exists()) {
            System.out.println("Could not find pom for " + file.getName());
            return new HashSet<>();
        }
        Set<Dependency> dependencies = new HashSet<>();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        Document document;
        try {
            document = dBuilder.parse(pom);
        } catch (IOException | SAXException e) {
            System.out.println("Could not parse pom for " + file.getName());
            return new HashSet<>();
        }
        document.getDocumentElement().normalize();
        Set<String> repositories = new HashSet<>();
        NodeList nodeList;
        if (document.getElementsByTagName("repository").getLength() != 0) {
            nodeList = document.getElementsByTagName("repository");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (element.getElementsByTagName("url").getLength() == 0 || element.getElementsByTagName("url").item(0) == null) {
                        continue;
                    }
                    String url = element.getElementsByTagName("url").item(0).getTextContent();
                    repositories.add(url);
                }
            }
        }
        repositories.add("https://repo1.maven.org/maven2/");
        nodeList = document.getElementsByTagName("dependency");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String groupId = null;
                String artifactId = null;
                String version = null;
                if (!(element.getElementsByTagName("groupId").getLength() == 0 || element.getElementsByTagName("groupId").item(0) == null)) {
                    groupId = element.getElementsByTagName("groupId").item(0).getTextContent();
                }
                if (!(element.getElementsByTagName("artifactId").getLength() == 0 || element.getElementsByTagName("artifactId").item(0) == null)) {
                    artifactId = element.getElementsByTagName("artifactId").item(0).getTextContent();
                }
                if (!(element.getElementsByTagName("version").getLength() == 0 || element.getElementsByTagName("version").item(0) == null)) {
                    version = element.getElementsByTagName("version").item(0).getTextContent();
                }
                if (groupId == null || artifactId == null || version == null) {
                    continue;
                }
                if (version.contains("${")) {
                    String property = version.substring(version.indexOf("${") + 2, version.indexOf("}"));
                    if (!(document.getElementsByTagName(property).getLength() == 0 || document.getElementsByTagName(property).item(0) == null)) {
                        version = version.replace("${" + property + "}", document.getElementsByTagName(property).item(0).getTextContent());
                    }
                }
                if (groupId.contains("${")) {
                    String property = groupId.substring(groupId.indexOf("${") + 2, groupId.indexOf("}"));
                    if (!(document.getElementsByTagName(property).getLength() == 0 || document.getElementsByTagName(property).item(0) == null)) {
                        groupId = groupId.replace("${" + property + "}", document.getElementsByTagName(property).item(0).getTextContent());
                    }
                }
                if (artifactId.contains("${")) {
                    String property = artifactId.substring(artifactId.indexOf("${") + 2, artifactId.indexOf("}"));
                    if (!(document.getElementsByTagName(property).getLength() == 0 || document.getElementsByTagName(property).item(0) == null)) {
                        artifactId = artifactId.replace("${" + property + "}", document.getElementsByTagName(property).item(0).getTextContent());
                    }
                }
                Dependency subDependency = new Dependency(this, groupId, artifactId, version, repositories);
                dependencies.add(subDependency);
            }
        }
        System.out.println("Found " + dependencies + " for " + file.getAbsolutePath());
        return dependencies;
    }

    private File getFromRepository(String repository, String groupId, String artifactId, String version) {
        String url = repository + groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".jar";
        String[] split = groupId.split("\\.");
        File file = mavenDirectory;
        File temp = new File(skidDirectory, "temp");
        temp.mkdirs();
        for (String s : split) {
            file = new File(file + File.separator + s);
        }
        file = new File(file + File.separator + artifactId + File.separator + version);
        file = new File(file + File.separator + artifactId + "-" + version + ".jar");
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.connect();
            temp = new File(temp, artifactId + "-" + version + ".jar");
            if (temp.exists()) {
                temp.delete();
            }
            temp.createNewFile();
            try (InputStream inputStream = connection.getInputStream()) {
                Files.copy(inputStream, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            try{
                new ZipFile(temp);
            } catch (ZipException e) {
                return null;
            }
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            File finalFile = file;
            connection.getInputStream().close();

            connection = new URL(url.replace(".jar", ".pom")).openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.connect();

            temp = new File(temp.getParentFile(), artifactId + "-" + version + ".pom");
            if (temp.exists()) {
                temp.delete();
            }

            temp.createNewFile();
            try (InputStream inputStream = connection.getInputStream()) {
                Files.copy(inputStream, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            if (temp.length() == 0) {
                return null;
            }

            file = new File(file.getParentFile(), artifactId + "-" + version + ".pom");
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Downloaded " + groupId + ":" + artifactId + ":" + version + " from " + url);
            return finalFile;
        } catch (IOException e) {
            return null;
        }
    }

    private File getFromLocalRepository(String groupId, String artifactId, String version) {
        String[] split = groupId.split("\\.");
        File file = mavenDirectory;
        for (String s : split) {
            file = new File(file + File.separator + s);
        }
        file = new File(file + File.separator + artifactId + File.separator + version);
        File[] files = file.listFiles();
        if (files == null) {
            return null;
        }
        for (File f : files) {
            if (f.getName().endsWith(".jar")) {
                try {
                    new ZipFile(f);
                } catch (ZipException e) {
                    System.out.println("Corrupted file: " + f.getAbsolutePath());
                    f.delete();
                    continue;
                } catch (IOException e) {
                    continue;
                }
                return f;
            }
        }
        return null;
    }
}
