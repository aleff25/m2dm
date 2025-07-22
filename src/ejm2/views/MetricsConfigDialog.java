package ejm2.views;

import java.awt.TextField;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ejm2.tools.FileUtils;
import ejm2.tools.Metric;

public class MetricsConfigDialog extends Dialog {
	private List<Metric> selectedMetrics;
    private List<Button> metricButtons;
    private Set<Metric> customMetrics;

    private String useFilePath;
    private Text metricNameText;
    private Text metricOclText;
    private Combo metricTypeCombo;
    private Composite metricsContainer;
    private ScrolledComposite scrolledComposite;
    private Button addMetricButton;
    private InputStream useFileInputStream;
    private OutputStream useFileOutputStream;

    public MetricsConfigDialog(Shell parentShell, 
    		Collection<Metric> metrics, 
    		InputStream useFileInputStream, 
    		OutputStream useFileOutputStream,
    		String useFilePath) {
        super(parentShell);
        this.selectedMetrics = new ArrayList<>(metrics);
        this.metricButtons = new ArrayList<>();
        this.customMetrics = new HashSet<>();
        this.useFileInputStream = useFileInputStream;
        this.useFileOutputStream = useFileOutputStream;
        this.useFilePath = useFilePath;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Metrics Configuration");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
    	Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(1, false));

        createScrollableMetricSection(container);
        createCustomMetricSection(container);

        return container;
    }
    
    private void createScrollableMetricSection(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText("Select the existing metrics:");

        scrolledComposite = new ScrolledComposite(parent, SWT.BORDER | SWT.V_SCROLL);
        scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);

        metricsContainer = new Composite(scrolledComposite, SWT.NONE);
        metricsContainer.setLayout(new GridLayout(2, false));

        for (Metric metric : selectedMetrics) {
            Button button = new Button(metricsContainer, SWT.CHECK);
            button.setText(metric.toString());
            button.setSelection(metric.isActive);
            button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
            metricButtons.add(button);
        }

        scrolledComposite.setContent(metricsContainer);
        metricsContainer.pack();
        scrolledComposite.setMinSize(metricsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    private void createCustomMetricSection(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText("Create a new metric:");
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        label.setLayoutData(gd);

        Label nameLabel = new Label(parent, SWT.NONE);
        nameLabel.setText("Name:");
        metricNameText = new Text(parent, SWT.BORDER);
        metricNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label typeLabel = new Label(parent, SWT.NONE);
        typeLabel.setText("Type:");
        metricTypeCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        metricTypeCombo.setItems(new String[]{"Class", "Method", "Package", "Project"});
        metricTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        
        Label oclLabel = new Label(parent, SWT.NONE);
        oclLabel.setText("OCL:");
        metricOclText = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        GridData oclTextGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        oclTextGridData.heightHint = 100;
        metricOclText.setLayoutData(oclTextGridData);
        
        addMetricButton = new Button(parent, SWT.PUSH);
        addMetricButton.setText("Add Metric");
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
            selectedMetrics.add(customMetric);
            customMetrics.add(customMetric);

            saveNewMetrics(Collections.singleton(customMetric));

            Button button = new Button(metricsContainer, SWT.CHECK);
            button.setText(customMetric.toString());
            button.setSelection(true);
            button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
            metricButtons.add(button);

            metricsContainer.layout(true, true);
            metricsContainer.pack();
            scrolledComposite.setMinSize(metricsContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));

            metricNameText.setText("");
            metricOclText.setText("");
            metricTypeCombo.deselectAll();
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
                if (i > 0 && lines.get(i - 1).contains("@metric" + metric.type)) {
                    lines.set(i - 1, "@metric" + metric.type + "(active = \"" + metric.getIsActive() + "\")");
                }
                metricUpdated = true;
                break;
            }
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
    
    private void saveNewMetrics(Set<Metric> metrics) {
    	try {
    		Path filePath = Paths.get(useFilePath);
    		List<String> metricsTransformed = metrics.stream().map(m -> m.transformToOCL()).collect(Collectors.toList());
        	String content = new String(Files.readAllBytes(filePath));
        	System.out.println(content);
        	String updateFiled = FileUtils.addCustomText(content, metricsTransformed);
        	Files.write(filePath, updateFiled.getBytes());
    	} catch (Exception e) {
    		e.printStackTrace();
		}
    }

    @Override
    protected void okPressed() {
        for (int i = 0; i < metricButtons.size(); i++) {
            Button button = metricButtons.get(i);
            Metric metric = getSelectedMetric(button.getText());
            if (metric != null) {
            	metric.isActive = button.getSelection();
                selectedMetrics.set(i, metric);
                saveMetricToFile(metric);
            }
        }
        
        super.okPressed();
    }

    public List<Metric> getSelectedMetrics() {
        return selectedMetrics.stream().filter(m -> m.isActive).collect(Collectors.toList());
    }
    
    public Metric getSelectedMetric(String metricName) {
        return selectedMetrics.stream().filter(m -> metricName.equals(m.toString())).findFirst().orElse(null);
    }

    public Set<Metric> getCustomMetrics() {
        return customMetrics;
    }

}
