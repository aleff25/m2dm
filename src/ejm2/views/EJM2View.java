package ejm2.views;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import ejm2.ui.EJM2ActionGroup;



public class EJM2View extends ViewPart {

	private Label label, label2;
	private Combo projectSelector;
	private Text input, output;
	private EJM2ActionGroup ag;
	private Button loadUSE, loadEJMM;
	private TreeViewer viewer;

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
	    
	    ClassNode mainClass = new ClassNode("Application", 300, 10, 80.0, 5, true);
	    root.addClass(mainClass);
	    
	    ClassNode otherClass = new ClassNode("Helper", 150, 5, 60.0, 3, false);
	    subPackage.addClass(otherClass);
	    
	    return new Object[] { root };
    }
	
	private void createColumns() {
		String[] titles = {"Package/Class", "LOC", "Methods", "Classes", "Coverage", "Complexity"};
		int[] bounds = {250, 100, 100, 100, 100, 100};
		
		TreeViewerColumn mainColumn = new TreeViewerColumn(viewer, SWT.NONE);
		mainColumn.getColumn().setText(titles[0]);
		mainColumn.getColumn().setWidth(bounds[0]);
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
		
		for (int i = 1; i < titles.length; i++) {
			TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.RIGHT);
			column.getColumn().setText(titles[i]);
			column.getColumn().setWidth(bounds[i]);
			column.getColumn().setResizable(true);
			
			final int index = i;
			column.setLabelProvider(new ColumnLabelProvider() {
				public String getText(Object element) {
					if (element instanceof ClassNode) {
						ClassNode node = (ClassNode) element;
						switch(index) {
							case 1: return String.valueOf(node.loc);
							case 2: return String.valueOf(node.methods);
							case 3: return "";
							case 4: return node.coverage + "%";
							case 5: return String.valueOf(node.complexity);
						}
					}
					return "";
				}
			});
		}
		
	}
	
	public void updateTree(IJavaProject javaProject) {
        if (javaProject != null) {
            PackageNode root = new PackageNode(javaProject.getElementName());
            findClasses(javaProject, root);
            viewer.setInput(new Object[] { root });
            viewer.refresh();
        }
    }
	
	public void findClasses(IJavaProject javaProject, PackageNode rootNode) {
		try {
	        for (IPackageFragment pack : javaProject.getPackageFragments()) {
	            if (pack.getKind() == IPackageFragmentRoot.K_SOURCE && !isTestPackage(pack.getElementName())) {
	                List<ClassNode> classNodes = new ArrayList<>();
	                for (ICompilationUnit unit : pack.getCompilationUnits()) {
	                    for (IType type : unit.getAllTypes()) {
	                        if (!type.isInterface()) {
	                            boolean isMain = containsMainMethod(type);
	                            classNodes.add(new ClassNode(type.getElementName(), calculateLOC(type), countMethods(type), calculateCoverage(type), calculateComplexity(type), isMain));
	                        }
	                    }
	                }
	                if (!classNodes.isEmpty()) {
	                    PackageNode packageNode = findOrCreatePackageNode(rootNode, pack.getElementName());
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
	
	private int calculateLOC(IType type) {
	    // Implementar cálculo de LOC
	    return 0;
	}

	private int countMethods(IType type) throws JavaModelException {
	    // Conta os métodos na classe
	    return type.getMethods().length;
	}

	private double calculateCoverage(IType type) {
	    // Implementar cálculo de cobertura de teste
	    return 0.0;
	}

	private int calculateComplexity(IType type) {
	    // Implementar cálculo de complexidade
	    return 0;
	}
	
	@Override
	public void setFocus() {
	}

}
