package ejm2.views;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.swing.text.Position;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.quasar.juse.api.JUSE_ProgramingFacade;
import org.tzi.use.uml.mm.MElementAnnotation;
import org.tzi.use.uml.mm.MOperation;
import org.tzi.use.uml.sys.MObject;

import com.google.common.collect.Lists;

import ejm2.tools.Metric;
import ejm2.tools.PluginDirectoryUtil;
import ejm2.ui.EJM2ActionGroup;



public class EJM2View extends ViewPart {

	private Label label, label2;
	private Combo projectSelector;
	private Text input, output;
	private EJM2ActionGroup ag;
	private Button configButton;
	private TreeViewer viewer;
	private List<Metric> activeMetrics = new ArrayList<>();
	private Set<Metric> allMetrics = new HashSet<>();
	private String projectName;
	private PackageNode root = new PackageNode("root");

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayoutData(new GridData(GridData.FILL, SWT.CENTER, true, false));
		c.setLayout(new GridLayout(1, false));
		
		label = new Label(c, SWT.NONE);
		label.setText("Press the Run JM2Loader button to instantiate a Java project's metamodel");

		label2 = new Label(c, SWT.NONE);
		label2.setText("Select the Java project to load");
		
		configButton = new Button(c, SWT.PUSH);
		configButton.setToolTipText("Select the folder where USE is located");
		Image image = new Image(c.getShell().getDisplay(), 
		          getClass().getClassLoader().getResourceAsStream("icons/gear.png"));
		configButton.setImage(image);
		

        configButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        configButton.addSelectionListener(new SelectionAdapter() {
           public void widgetSelected(SelectionEvent e) {
               openMetricsConfigDialog();
           }
        });
 
		Button exportButton = new Button(c, SWT.PUSH);
        exportButton.setText("Export report");
        exportButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        exportButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                exportReport();
            }
        });

		projectSelector = new Combo(c, SWT.READ_ONLY | SWT.DROP_DOWN);

		
		viewer = new TreeViewer(parent, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);
		viewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		createColumns();
		viewer.setInput(getInitialInput());
		
		ag = new EJM2ActionGroup(this);
		IActionBars actionBars = getViewSite().getActionBars();
		ag.fillActionBars(actionBars); 
	}

	public void setLabelText(String s){
		label.setText(s);
	}
	
	public void setLabel2Text(String s){
		label2.setText(s);
	}
	
	public Combo getProjectSelector(){
		return projectSelector;
	}
	
	public Text getOutputEditor(){
		return output;
	}
	
	public Text getInputEditor(){
		return input;
	}
	
	
	public TreeViewer getViewer() {
		return viewer;
	}
	
	public Object getInitialInput() {
		PackageNode root = new PackageNode("com.example");
	    PackageNode subPackage = new PackageNode("service");
	    root.addChild(subPackage);
	    
	    return new Object[] { root };
    }
	
	private void createColumns() {
		
		List<Metric> metrics = createMetrics();
		
		TreeViewerColumn mainColumn = new TreeViewerColumn(viewer, SWT.NONE);
		mainColumn.getColumn().setText(metrics.get(0).toString());
		mainColumn.getColumn().setWidth(250);
		mainColumn.getColumn().setResizable(true);
		mainColumn.setLabelProvider(new ColumnLabelProvider( ) {
			public String getText(Object element) {
				if (element instanceof PackageNode) {
					PackageNode node = ((PackageNode) element);
					return node.containsMain ? node.name + " (main)" : node.name;
				} else if(element instanceof MethodNode) {
					return ((MethodNode) element).nameMethod;
				} else if(element instanceof ClassNode) {
					return ((ClassNode) element).name;
				}
				return "";
			}
		});
		
		if (activeMetrics.size() == 0) {
			
			for (int i = 1; i < metrics.size(); i++) {
				TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.CENTER);
				column.getColumn().setText(metrics.get(i).toString());
				column.getColumn().setWidth(100);
				column.getColumn().setResizable(true);
				
				final int index = i;
				column.setLabelProvider(new ColumnLabelProvider() {
					public String getText(Object element) {
						if (element instanceof ClassNode && ((ClassNode) element).metrics.size() > 0) {
							ClassNode node = (ClassNode) element;
							return node.getMetricByKey(metrics.get(index).toString()).toString();
						} else if(element instanceof PackageNode && ((PackageNode) element).metrics.size() > 0) {
							PackageNode node = (PackageNode) element;
							return node.getMetricByKey(metrics.get(index).toString()).toString();
						}
						return "";
					}
				});
				
				column.getColumn().setAlignment(SWT.CENTER);
			}
		} else {
			
			for (Metric metric : activeMetrics) {
				TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.CENTER);
				column.getColumn().setText(metric.toString());
				column.getColumn().setWidth(100);
				column.getColumn().setResizable(true);
				column.getColumn().setAlignment(SWT.CENTER);
				
				final String k = metric.toString();
				column.setLabelProvider(new ColumnLabelProvider() {
					public String getText(Object element) {
						if (element instanceof MethodNode ) {
							MethodNode node = (MethodNode) element;
							return node.getMethodMetric(k);
						} else if(element instanceof PackageNode) {
							PackageNode node = (PackageNode) element;
							return node.getMetricByKey(k);
						} else if(element instanceof ClassNode) {
							ClassNode node = (ClassNode) element;
							return node.getMetricByKey(k);
						}
						return "";
					}
				});
			}
		}
		
	}
	
	private List<Metric> createMetrics() {
		List<String> titles = Lists.newArrayList("Package/Class", "LOC", "Methods", "Classes", "Coverage", "Complexity");
		List<Metric> metrics = new ArrayList<Metric>();
		
		for (String metricName : titles) {
			metrics.add(new Metric(metricName, "", "", "true"));
		}
		
		return metrics;
	}
	
	public void updateTree(IJavaProject javaProject, JUSE_ProgramingFacade api) {
        if (javaProject != null) {
            PackageNode root = new PackageNode(javaProject.getElementName());
            this.root = root;
            findClasses(javaProject, api, root);
           
            Tree tree = viewer.getTree();
            for (TreeColumn column : tree.getColumns()) {
                column.dispose();
            }

            createColumns(activeMetrics);
            viewer.setInput(new Object[] { root });
            viewer.refresh();
        }
    }
	
	public void findClasses(IJavaProject javaProject, JUSE_ProgramingFacade api, PackageNode rootNode) {
		try {
	        for (IPackageFragment pack : javaProject.getPackageFragments()) {
	            if (pack.getKind() == IPackageFragmentRoot.K_SOURCE && !isTestPackage(pack.getElementName())) {
                    PackageNode packageNode = findOrCreatePackageNode(rootNode, pack.getElementName());
	                List<ClassNode> classNodes = new ArrayList<>();
	                List<Metric> metricsPackage = new ArrayList<>();
                	
	                String[] parts = pack.getElementName().split("\\.");
	                String packageName = parts[parts.length - 1];
	                
                    Optional<MObject> mObjectPackagesOpt = api.allObjects().stream()
                    		.filter(obj -> obj.name().endsWith(packageName)).findFirst();
                    

                    if (mObjectPackagesOpt.isPresent()) {
                    	for (MOperation operation : mObjectPackagesOpt.get().cls().allOperations()) {
		                    if (operation.getAllAnnotations().values()
		            				.stream().anyMatch(annotation -> annotation.getName().equals("metricPackage"))) {
		            			String key = operation.name();
								String value = api.oclEvaluator(mObjectPackagesOpt.get().name() + "." + operation.name() + "()").toString();
								MElementAnnotation annotation = operation.getAllAnnotations().values().stream()
										.filter(a -> a.getName().equals("metricPackage"))
										.findFirst().get();
		    					boolean isActive = annotation.getAnnotationValue("active").equals("true");
								Metric metric = new Metric(key, value, "Package", isActive);
								if (metric.isActive) {
									metricsPackage.add(metric);
								}
								this.allMetrics.add(metric);
		            		}
                    	}
                    }
                    
	                for (ICompilationUnit unit : pack.getCompilationUnits()) {
	                    for (IType type : unit.getAllTypes()) {
	                        if (!type.isInterface()) {
	                            boolean isMain = containsMainMethod(type);
	                            
	                            MObject mObject = api.allObjects().stream().filter(obj -> obj.name().endsWith(type.getElementName())).findFirst().get();
	                            
	                            if (mObject != null) {
	                            	List<Metric> metricsClass = new ArrayList<Metric>();

	                            	
	                            	for (MOperation operation : mObject.cls().allOperations()) {
	                            		
	                            		Collection<MElementAnnotation> annotations = operation.getAllAnnotations().values();
	                            		if (annotations.stream().anyMatch(annotation -> annotation.getName().equals("metricClass"))) {
	                            			String key = operation.name();
											String value = api.oclEvaluator(mObject.name() + "." + operation.name() + "()").toString();
											MElementAnnotation annotation = annotations.stream()
													.filter(a -> a.getName().equals("metricClass"))
													.findFirst().get();
					    					boolean isActive = annotation.getAnnotationValue("active").equals("true");
											Metric metric = new Metric(key, value, "Class", isActive);
											if (metric.isActive) {
												metricsClass.add(metric);
											}
											this.allMetrics.add(metric);
	                            		}
									}
	                            	
	                            	ClassNode classNode = new ClassNode(type.getElementName(), isMain, metricsClass);
		                            classNodes.add(classNode);
		                            
		                            if (classNodes.size() == 1) {
		                            	this.activeMetrics = new ArrayList<Metric>();
		                            	this.activeMetrics.addAll(metricsPackage);
		                            	this.activeMetrics.addAll(metricsClass);
		                            }
	                            }
	                            
	                        }
	                        

	                    	List<MethodNode> methods = findMethods(type, api);
	                    	classNodes.addAll(methods);
	                    }
	                    
	                    
	                }
	                if (!classNodes.isEmpty()) {

	                	packageNode.addMetrics(metricsPackage, api);
	                    for (ClassNode classNode : classNodes) {
	                        packageNode.addClass(classNode);
	                        if (classNode.isMain) {
	                            packageNode.setMainClazz();
	                        }
	                    }
	                }
	            }
	        }
	    } catch (JavaModelException e) {
	        e.printStackTrace();
	    }
	}
	
	private List<MethodNode> findMethods(IType type, JUSE_ProgramingFacade api) {
		List<MethodNode> methodsNode = new ArrayList<MethodNode>();
		Set<Metric> methodsMetrics = new HashSet<Metric>();
	    try {
	        for (IMethod method : type.getMethods()) {
	        	List<Metric> methodsMetricsList = new ArrayList<Metric>();

                MObject mObject = api.allObjects().stream().filter(obj -> obj.name().startsWith("METHOD") && 
                		obj.name().contains(method.getElementName())).findFirst().get();

                for (MOperation operation : mObject.cls().allOperations()) {
            		
            		Collection<MElementAnnotation> annotations = operation.getAllAnnotations().values();
            		if (annotations
            				.stream().anyMatch(annotation -> annotation.getName().equals("metricMethod"))) {
            			String key = operation.name();
    					String value = api.oclEvaluator(mObject.name() + "." + operation.name() + "()").toString();
    					MElementAnnotation annotation = annotations.stream()
								.filter(a -> a.getName().equals("metricMethod"))
								.findFirst().get();
    					boolean isActive = annotation.getAnnotationValue("active").equals("true");
    					Metric metric = new Metric(key, value, "Method", isActive);
    					if (metric.isActive) {
    						methodsMetrics.add(metric);
						}
    					methodsMetricsList.add(metric);
						this.allMetrics.add(metric);
            		} 
				}
                
	            MethodNode methodNode = new MethodNode(method.getElementName(), methodsMetricsList);
	            methodNode.setParent(type.getClass());
	            methodsNode.add(methodNode);
	        }
	    } catch (JavaModelException e) {
	        e.printStackTrace();
	    }
	    this.activeMetrics.addAll(methodsMetrics);
	    return methodsNode;
	}
	
	private boolean isTestPackage(String packageName) {
	    return packageName.contains(".test.") || packageName.contains(".tests.") || packageName.endsWith(".test") || packageName.endsWith(".tests");
	}
	
	private boolean isTestClazz(String clazzName) {
		return clazzName.toLowerCase().endsWith("test");
	}
	
	private boolean containsMainMethod(IType type) throws JavaModelException {
	    // Verifica se algum método é o método 'main'
	    for (IMethod method : type.getMethods()) {
	        if (isMainMethod(method)) {
	            return true;
	        }
	    }
	    return false;
	}

	private boolean isMainMethod(IMethod method) throws JavaModelException {
	    return method.isMainMethod();
	}

	private PackageNode findOrCreatePackageNode(PackageNode parent, String fullPackageName) {
		String[] parts = fullPackageName.split("\\.");
	    PackageNode current = parent;
	    for (String part : parts) {
	        current = current.findOrAddChild(part);
	    }
	    return current;
	}

	private void exportReport() {
	    FileDialog dialog = new FileDialog(getSite().getShell(), SWT.SAVE);
	    dialog.setFilterNames(new String[] { "Excel Files", "All Files (*.*)" });
	    dialog.setFilterExtensions(new String[] { "*.xlsx", "*.*" });
	    dialog.setFileName("report.csv");
	    String path = dialog.open();
	    if (path != null) {
	    	generateCSVReport(path);
	        MessageBox messageBox = new MessageBox(getSite().getShell(), SWT.ICON_INFORMATION | SWT.OK);
	        messageBox.setMessage("Exported report succesfully to " + path);
	        messageBox.open();
	    }
	}

	private void generateCSVReport(String path) {
	    String[] headers = { "System name", "Version", "Package(location)", "Class", "Metric name", "Metric method", "Metric value" };
	    try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
	        // Write headers
	        writer.write(String.join(",", headers));
	        writer.newLine();

	        // Write data
	        Object[] elements = (Object[]) viewer.getInput();
	        if (elements != null) {
	            for (Object element : elements) {
	                appendCSVRows(writer, element);
	            }
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	private void appendCSVRows(BufferedWriter writer, Object element) throws IOException {
		if (element instanceof PackageNode) {	
	        PackageNode node = (PackageNode) element;
	        
	        if (node.metrics.size() > 0) {
	        	for (Metric metric : node.metrics) {
	            	writer.write(String.join(",",
	                        "m2md", // System name
	                        "v1.1", // Version
	                        node.getClass().getPackage().getName(), // Package(Location)
	                        node.name, // Class
	                        metric.name, //  Metric name
	                        metric.type, // Metric method
	                        metric.ocl // Metric value
	                ));
	                writer.newLine();
	            }
	        }
	        
	        for (PackageNode child : node.children) {
	            appendCSVRows(writer, child);
	        }
	        for (ClassNode cls : node.classes) {
	            appendCSVRows(writer, cls);
	        }
		}
        else if (element instanceof MethodNode) {
	    	MethodNode node = (MethodNode) element;
            for (Metric metric : node.metricsMethod) {
            	writer.write(String.join(",",
                        "m2md", // System name
                        "v1.1", // Version
                        node.getClass().getPackage().getName(), // Package(Location)
                        node.name + " - " + node.nameMethod, // Class
                        metric.name, //  Metric name
                        metric.type, // Metric method
                        metric.ocl // Metric value
                ));
                writer.newLine();
            }
		    
	    } else if (element instanceof ClassNode) {
	    	ClassNode node = (ClassNode) element;
            for (Metric metric : node.metrics) {
            	writer.write(String.join(",",
                        "m2md", // System name
                        "v1.1", // Version
                        node.getClass().getPackage().getName(), // Package(Location)
                        node.name, // Class
                        metric.name, //  Metric name
                        metric.type, // Metric method
                        metric.ocl // Metric value
                ));
                writer.newLine();
            }
	    }
	}
    
	private void openMetricsConfigDialog() {
    	List<Metric> metrics = new ArrayList<>();
        metrics.addAll(allMetrics);

        String path = new File("").getAbsolutePath();
        String workspace = PluginDirectoryUtil.getPluginDirectory("m2dm").getAbsolutePath();
        String useFilePath = workspace + "/lib/JavaMMv5_FLAME.use";
        
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("lib/JavaMMv5_FLAME.use");
            OutputStream outputStream = new FileOutputStream(useFilePath, true)) {
           MetricsConfigDialog dialog = new MetricsConfigDialog(getSite().getShell(), metrics, inputStream, outputStream, useFilePath);
           if (dialog.open() == Dialog.OK) {
        	   metrics = dialog.getSelectedMetrics();
               Tree tree = viewer.getTree();
               for (TreeColumn column : tree.getColumns()) {
                   column.dispose();
               }
               createColumns(metrics);
               viewer.setInput(new Object[] { root });
               viewer.refresh();
           }
       } catch (IOException e) {
           e.printStackTrace();
       }
    }
    
    private void createColumns(List<Metric> metrics) {
    	TreeViewerColumn mainColumn = new TreeViewerColumn(viewer, SWT.NONE);
		mainColumn.getColumn().setText("Package/Class");
		mainColumn.getColumn().setWidth(250);
		mainColumn.getColumn().setResizable(true);
		mainColumn.setLabelProvider(new ColumnLabelProvider( ) {
			public String getText(Object element) {
				if (element instanceof PackageNode) {
					PackageNode node = ((PackageNode) element);
					return node.containsMain ? node.name + " (main)" : node.name;
				} else if(element instanceof MethodNode) {
					return ((MethodNode) element).nameMethod;
				} else if(element instanceof ClassNode) {
					return ((ClassNode) element).name;
				}
				return "";
			}
		});
		
		for (Metric metric : metrics) {
			TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.CENTER);
			column.getColumn().setText(metric.toString());
			column.getColumn().setWidth(100);
			column.getColumn().setResizable(true);
			column.getColumn().setAlignment(SWT.CENTER);
			
			final String k = metric.toString();
			column.setLabelProvider(new ColumnLabelProvider() {
				public String getText(Object element) {
					if (element instanceof MethodNode ) {
						MethodNode node = (MethodNode) element;
						return node.getMethodMetric(k);
					} else if(element instanceof PackageNode) {
						PackageNode node = (PackageNode) element;
						return node.getMetricByKey(k);
					} else if(element instanceof ClassNode) {
						ClassNode node = (ClassNode) element;
						return node.getMetricByKey(k);
					}
					return "";
				}
			});
		}
    }
	
	@Override
	public void setFocus() {
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	
	private String getCurrentProject() {
        return PluginDirectoryUtil.getPluginDirectory("m2dm").getAbsolutePath();
    }

}
