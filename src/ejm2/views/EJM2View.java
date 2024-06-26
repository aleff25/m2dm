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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.text.Position;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.quasar.juse.api.JUSE_ProgramingFacade;
import org.tzi.use.uml.mm.MOperation;
import org.tzi.use.uml.sys.MObject;

import com.google.common.collect.Lists;

import ejm2.ui.EJM2ActionGroup;



public class EJM2View extends ViewPart {

	private Label label, label2;
	private Combo projectSelector;
	private Text input, output;
	private EJM2ActionGroup ag;
	private Button loadUSE, loadEJMM;
	private TreeViewer viewer;
	private Map<String, List<String>> columnNames = new HashMap<>();

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
		loadUSE = new Button(c, SWT.PUSH);
		loadUSE.setText("Select USE directory");
		loadUSE.setToolTipText("Select the folder where USE is located");
		
		loadEJMM = new Button(c, SWT.PUSH);
		loadEJMM.setText("Select EJMM path");
		loadEJMM.setToolTipText("Select the location of the EJMM .use file");
		
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
	
	public Button getLoadUSE() {
		return loadUSE;
	}

	public Button getLoadEJMM() {
		return loadEJMM;
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
		
		List<String> titles = Lists.newArrayList("Package/Class", "LOC", "Methods", "Classes", "Coverage", "Complexity");
		
		TreeViewerColumn mainColumn = new TreeViewerColumn(viewer, SWT.NONE);
		mainColumn.getColumn().setText(titles.get(0));
		mainColumn.getColumn().setWidth(250);
		mainColumn.getColumn().setResizable(true);
		mainColumn.setLabelProvider(new ColumnLabelProvider( ) {
			public String getText(Object element) {
				if (element instanceof PackageNode) {
					PackageNode node = ((PackageNode) element);
					return node.containsMain ? node.name + " (main)" : node.name;
				} else if(element instanceof ClassNode) {
					return ((ClassNode) element).name;
				}
				return "";
			}
		});
		
		if (columnNames.size() == 0) {
			
			for (int i = 1; i < titles.size(); i++) {
				TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.CENTER);
				column.getColumn().setText(titles.get(i));
				column.getColumn().setWidth(100);
				column.getColumn().setResizable(true);
				
				final int index = i;
				column.setLabelProvider(new ColumnLabelProvider() {
					public String getText(Object element) {
						if (element instanceof ClassNode && ((ClassNode) element).metrics.size() > 0) {
							ClassNode node = (ClassNode) element;
							return node.metrics.get(titles.get(index)).toString();
						} else if(element instanceof PackageNode && ((PackageNode) element).metrics.size() > 0) {
							PackageNode node = (PackageNode) element;
							return node.metrics.get(titles.get(index)).toString();
						}
						return "";
					}
				});
				
				column.getColumn().setAlignment(SWT.CENTER);
			}
		} else {
			for (String key: columnNames.get("metricsPackage")) {
				TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.CENTER);
				column.getColumn().setText(key);
				column.getColumn().setWidth(100);
				column.getColumn().setResizable(true);
				column.getColumn().setAlignment(SWT.CENTER);
				
				final String k = key;
				column.setLabelProvider(new ColumnLabelProvider() {
					public String getText(Object element) {
						if (element instanceof ClassNode ) {
							ClassNode node = (ClassNode) element;
							return node.getMetricByKey(k);
						} else if(element instanceof PackageNode) {
							PackageNode node = (PackageNode) element;
							return node.getMetricByKey(k);
						}
						return "";
					}
				});
				
			}
			
			for (String key: columnNames.get("metricsClass")) {
				TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.RIGHT);
				column.getColumn().setText(key);
				column.getColumn().setWidth(100);
				column.getColumn().setResizable(true);
				column.getColumn().setAlignment(SWT.CENTER);
				
				final String k = key;
				column.setLabelProvider(new ColumnLabelProvider() {
					public String getText(Object element) {
						if (element instanceof ClassNode) {
							ClassNode node = (ClassNode) element;
							return node.getMetricByKey(k);
						} else if(element instanceof PackageNode) {
							PackageNode node = (PackageNode) element;
							return node.getMetricByKey(k);
						}
						return "";
					}
				});
				
			}
		}
		
	}
	
	public void updateTree(IJavaProject javaProject, JUSE_ProgramingFacade api) {
        if (javaProject != null) {
            PackageNode root = new PackageNode(javaProject.getElementName());
            findClasses(javaProject, api, root);
           
            // Dispose of existing columns
            Tree tree = viewer.getTree();
            for (TreeColumn column : tree.getColumns()) {
                column.dispose();
            }

            // Recreate initial columns
            
            createColumns();
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
	                Map<String, String> metricsPackage = new HashMap<>();
	                
	                String[] parts = pack.getElementName().split("\\.");
	                String packageName = parts[parts.length - 1];
	                
                    Optional<MObject> mObjectPackagesOpt = api.allObjects().stream()
                    		.filter(obj -> obj.name().endsWith(packageName)).findFirst();
                    

                    if (mObjectPackagesOpt.isPresent()) {
                    	for (MOperation operation : mObjectPackagesOpt.get().cls().allOperations()) {
		                    if (operation.getAllAnnotations().values()
		            				.stream().anyMatch(annotation -> annotation.getName().equals("metricsPackage"))) {
		            			String key = operation.name();
								metricsPackage.put(key, "");
		            		}
                    	}
                    }
                    
	                for (ICompilationUnit unit : pack.getCompilationUnits()) {
	                    for (IType type : unit.getAllTypes()) {
	                        if (!type.isInterface()) {
	                            boolean isMain = containsMainMethod(type);
	                            
	                            MObject mObject = api.allObjects().stream().filter(obj -> obj.name().endsWith(type.getElementName())).findFirst().get();
	                            
	                            if (mObject != null) {
	                            	Map<String, Object> map = new HashMap<String, Object>();
	                            	
	                            	for (MOperation operation : mObject.cls().allOperations()) {
	                            		
	                            		if (operation.getAllAnnotations().values()
	                            				.stream().anyMatch(annotation -> annotation.getName().equals("metricsClass"))) {
	                            			String key = operation.name();
											Object value = api.oclEvaluator(mObject.name() + "." + operation.name() + "()").toString();
											map.put(key, value);
	                            		}
									}
	                            	
		                            classNodes.add(new ClassNode(type.getElementName(), isMain, map));
		                            
		                            if (classNodes.size() == 1) {
		                            	List<String> oprNames = new ArrayList<>();
		                            	for (MOperation operation : mObject.cls().allOperations()) {
		                            		
		                            		if (operation.getAllAnnotations().values()
		                            				.stream().anyMatch(annotation -> annotation.getName().equals("metricsClass"))) {
		                            			oprNames.add(operation.name());
		                            		}
										}
		                            	this.columnNames = new HashMap<>();
		                            	this.columnNames.put("metricsPackage", new ArrayList<>(metricsPackage.keySet()));
		                            	this.columnNames.put("metricsClass", oprNames);
		                            }
	                            }
	                            
	                        }
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
	
	private boolean isTestPackage(String packageName) {
	    // Verifica se o nome do pacote sugere que é um pacote de teste
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
        dialog.setFilterNames(new String[] { "HTML Files", "All Files (*.*)" });
        dialog.setFilterExtensions(new String[] { "*.html", "*.*" });
        dialog.setFileName("relatorio.html");
        String path = dialog.open();
        if (path != null) {
            generateHtmlReport(path);
            MessageBox messageBox = new MessageBox(getSite().getShell(), SWT.ICON_INFORMATION | SWT.OK);
            messageBox.setMessage("Relatório exportado com sucesso para " + path);
            messageBox.open();
        }
    }

    private void generateHtmlReport(String path) {
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<html><head><title>Metrics Report</title></head><body>");
        htmlContent.append("<h1>Metrics Report</h1>");
        htmlContent.append("<table border='1'><tr><th>Name</th>");
        
        for (List<String> metricsList : columnNames.values()) {
        	for (String metricName : metricsList) {
                htmlContent.append("<th>").append(metricName).append("</th>");
            }
		}
        
        htmlContent.append("</tr>");

        Object[] elements = (Object[]) viewer.getInput();
        if (elements != null) {
            for (Object element : elements) {
                appendHtmlRows(htmlContent, element);
            }
        }

        htmlContent.append("</table></body></html>");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(htmlContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void appendHtmlRows(StringBuilder htmlContent, Object element) {
        if (element instanceof PackageNode) {
            PackageNode node = (PackageNode) element;
            htmlContent.append("<tr><td>").append(node.name).append("</td>");
            
            for (List<String> metricsList : columnNames.values()) {
            	for (String metricName : metricsList) {
                    htmlContent.append("<td>").append(node.getMetricByKey(metricName)).append("</td>");
                }
    		}
            
            htmlContent.append("</tr>");
            for (PackageNode child : node.children) {
                appendHtmlRows(htmlContent, child);
            }
            for (ClassNode cls : node.classes) {
                appendHtmlRows(htmlContent, cls);
            }
        } else if (element instanceof ClassNode) {
            ClassNode node = (ClassNode) element;
            htmlContent.append("<tr><td>").append("&nbsp;&nbsp;&nbsp;&nbsp;").append(node.name).append("</td>");
            
            for (List<String> metricsList : columnNames.values()) {
            	for (String metricName : metricsList) {
                    htmlContent.append("<td>").append(node.getMetricByKey(metricName)).append("</td>");
                }
    		}
            htmlContent.append("</tr>");
        }
    }
	
	@Override
	public void setFocus() {
	}

}
