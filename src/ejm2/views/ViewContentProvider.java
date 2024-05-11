package ejm2.views;

import java.util.stream.Stream;

import org.eclipse.jface.viewers.ITreeContentProvider;

public class ViewContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		return ((Object[]) inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof PackageNode) {
			PackageNode node = (PackageNode) parentElement;
			return Stream.concat(node.children.stream(), node.classes.stream()).toArray();
		}
		return new Object[0];
	}

	@Override
	public Object getParent(Object element) {
		if(element instanceof ClassNode) {
			return ((ClassNode) element).parent;
		} else if(element instanceof PackageNode) {
			return ((PackageNode) element).parent;
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof PackageNode) {
			PackageNode node = (PackageNode) element;
			return !node.children.isEmpty() || !node.classes.isEmpty();
		}
		return false;
	}

}
