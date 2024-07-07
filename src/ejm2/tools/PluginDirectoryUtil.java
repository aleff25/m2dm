package ejm2.tools;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

public class PluginDirectoryUtil {

	/**
     * Retorna o diretório de trabalho do plugin.
     *
     * @param pluginId o ID do plugin
     * @return o diretório de trabalho do plugin como um objeto File
     * @throws IOException se ocorrer um erro ao resolver o URL do bundle
     */
    public static File getPluginDirectory(String pluginId) {
        Bundle bundle = Platform.getBundle(pluginId);
        if (bundle == null) {
            throw new IllegalArgumentException("Plugin não encontrado: " + pluginId);
        }

        URL bundleURL = bundle.getEntry("/");
        try {
            URL fileURL = FileLocator.toFileURL(bundleURL);
            return new File(fileURL.getPath());
        } catch (Exception e) {
			System.out.println("Unable to set the path for the EJMMDirectory or/and useLocation");
		}

        return new File("");
    }
}
