package gg.codie.mineonline;

import gg.codie.utils.ArrayUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Map;

public class MinecraftServerProcess {
    public static final String PROP_AUTH_HOST = "minecraft.api.auth.host";
    public static final String PROP_ACCOUNT_HOST = "minecraft.api.account.host";
    public static final String PROP_SESSION_HOST = "minecraft.api.session.host";

    public static Process startMinecraftServer(String[] args) throws Exception {
        // Start the proxy as a new process.
        java.util.Properties props = System.getProperties();

        String[] launchArgs = new String[] {
                Settings.settings.getString(Settings.JAVA_COMMAND),
                Proxy.PROXY_SET_ARG,
                Proxy.PROXY_HOST_ARG,
                Proxy.PROXY_PORT_ARG + Proxy.getProxyPort(),
                "-javaagent:" + LauncherFiles.PATCH_AGENT_JAR,
                "-Djava.util.Arrays.useLegacyMergeSort=true",
                "-cp",
                new File(Proxy.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath(),
                MinecraftServerProcess.class.getCanonicalName()
        };

        ProcessBuilder processBuilder = new ProcessBuilder(ArrayUtils.concatenate(launchArgs, args));

        Map<String, String> env = processBuilder.environment();
        for(String prop : props.stringPropertyNames()) {
            env.put(prop, props.getProperty(prop));
        }
        processBuilder.directory(new File(System.getProperty("user.dir")));

        Process serverProcess = processBuilder.start();

        Thread closeLauncher = new Thread(() -> serverProcess.destroyForcibly());
        Runtime.getRuntime().addShutdownHook(closeLauncher);

        return serverProcess;
    }

    public static void main(String[] args) throws Exception {
        File jarFile = new File(args[0]);
        if(!jarFile.exists()) {
            System.err.println("Couldn't find jar file " + args[0]);
            System.exit(1);
        }

        LibraryManager.addJarToClasspath(Paths.get(args[0]).toUri().toURL());

        Class mainClass;

        try {
            mainClass = Class.forName("net.minecraft.server.Main");
        } catch (ClassNotFoundException ex) {
            mainClass = Class.forName("net.minecraft.server.MinecraftServer");
        }

        if (mainClass == null) {
            System.out.println("Main class not found!");
        }

        Method main = mainClass.getMethod("main", String[].class);

        System.setProperty(PROP_AUTH_HOST, "http://" + Globals.API_HOSTNAME);
        System.setProperty(PROP_ACCOUNT_HOST, "http://" + Globals.API_HOSTNAME);
        System.setProperty(PROP_SESSION_HOST, "http://" + Globals.API_HOSTNAME);

        main.invoke(null, new Object[] { new String[] {}});
    }
}
