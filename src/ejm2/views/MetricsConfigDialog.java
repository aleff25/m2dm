package ejm2.views;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ejm2.tools.Metric;

public class MetricsConfigDialog extends Dialog {
	private List<Metric> selectedMetrics;
    private List<Button> metricButtons;
    private List<Metric> customMetrics;

    private String useFilePath;
    private Text metricNameText;
    private Text metricOclText;
    private Combo metricTypeCombo;
    private Button addMetricButton;
    private InputStream useFileInputStream;
    private OutputStream useFileOutputStream;

    public MetricsConfigDialog(Shell parentShell, 
    		List<Metric> metrics, 
    		InputStream useFileInputStream, 
    		OutputStream useFileOutputStream,
    		String useFilePath) {
        super(parentShell);
        this.selectedMetrics = new ArrayList<>(metrics);
        this.metricButtons = new ArrayList<>();
        this.customMetrics = new ArrayList<>();
        this.useFileInputStream = useFileInputStream;
        this.useFileOutputStream = useFileOutputStream;
        this.useFilePath = useFilePath;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Configuração de Métricas");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(2, false));

        createMetricSelectionSection(container);
        createCustomMetricSection(container);

        return container;
    }

    private void createMetricSelectionSection(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText("Selecione Métricas Existentes:");
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        label.setLayoutData(gd);

        for (Metric metric : selectedMetrics) {
            Button button = new Button(parent, SWT.CHECK);
            button.setText(metric.toString());
            button.setSelection(metric.isActive);
            metricButtons.add(button);
            gd = new GridData();
            gd.horizontalSpan = 2;
            button.setLayoutData(gd);
        }
    }

    private void createCustomMetricSection(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText("Criar Nova Métrica:");
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        label.setLayoutData(gd);

        Label nameLabel = new Label(parent, SWT.NONE);
        nameLabel.setText("Nome:");
        metricNameText = new Text(parent, SWT.BORDER);
        metricNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label oclLabel = new Label(parent, SWT.NONE);
        oclLabel.setText("OCL:");
        metricOclText = new Text(parent, SWT.BORDER);
        metricOclText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label typeLabel = new Label(parent, SWT.NONE);
        typeLabel.setText("Tipo:");
        metricTypeCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        metricTypeCombo.setItems(new String[]{"Classe", "Método", "Pacote", "Projeto"});
        metricTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        addMetricButton = new Button(parent, SWT.PUSH);
        addMetricButton.setText("Adicionar Métrica");
        gd = new GridData();
        gd.horizontalSpan = 2;
        gd.horizontalAlignment = SWT.RIGHT;
        addMetricButton.setLayoutData(gd);
        addMetricButton.addListener(SWT.Selection, e -> addCustomMetric());
    }

    private void addCustomMetric() {
    	String name = metricNameText.getText();
        String ocl = metricOclText.getText();
        String type = metricTypeCombo.getText();

        if (!name.isEmpty() && !ocl.isEmpty() && !type.isEmpty()) {
            Metric customMetric = new Metric(name, ocl, type, "true");
            customMetrics.add(customMetric);

            Button button = new Button((Composite) getDialogArea(), SWT.CHECK);
            button.setText(customMetric.toString());
            button.setSelection(true);
            metricButtons.add(button);

            ((Composite) getDialogArea()).layout();

            metricNameText.setText("");
            metricOclText.setText("");
            metricTypeCombo.deselectAll();

            saveMetricToFile(customMetric);
        }
    }

    private void saveMetricToFile(Metric metric) {
    	// Read all metrics from the file
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(useFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        
        // Update the metric line in the list
        String metricLine = metric.name + " () : Integer = " + metric.ocl + "\n";
        boolean metricUpdated = false;
        for (int i = 0; i < lines.size(); i++) {
        	if (lines.get(i).contains(metric.name + " () :")) {
                if (i > 0 && lines.get(i - 1).contains("@metrics" + metric.type)) {
                    lines.set(i - 1, "@metrics" + metric.type + "(active = \"" + metric.getIsActive() + "\")");
                }
                metricUpdated = true;
                break;
            }
        }

        // If the metric was not found in the file, add it
        if (!metricUpdated && metric.isActive) {
            lines.add("@metrics" + metric.type + "(active = \"true\") \n");
            lines.add(metricLine);
        }

        // Write the updated metrics back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(useFilePath))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void okPressed() {
        for (int i = 0; i < metricButtons.size(); i++) {
            Button button = metricButtons.get(i);
            Metric metric = selectedMetrics.get(i);
            metric.isActive = button.getSelection();
            selectedMetrics.set(i, metric);
            saveMetricToFile(metric);
        }
        
        for (int i = 0; i < customMetrics.size(); i++) {
        	Metric metric = customMetrics.get(i);
        	if (metric.isActive) {
        		selectedMetrics.set(i, metric);
            }
        	saveMetricToFile(metric);
		}
        super.okPressed();
    }
    
    private String getMetricName(Button button) {
    	return button.getText().split("\\(")[0];
    }
    
    private String getType(String name) {
    	if (name.contains("Class")) {
    		return "Class";
    	} else if (name.contains("Method")) {
    		return "Method";
    	}  else if (name.contains("Package")) {
    		return "Package";
    	}  else {
    		return "Class";
    	}
    }

    public List<Metric> getSelectedMetrics() {
        return selectedMetrics.stream().filter(m -> m.isActive).collect(Collectors.toList());
    }

    public List<Metric> getCustomMetrics() {
        return customMetrics;
    }


}
