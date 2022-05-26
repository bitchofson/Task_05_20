package ru.vsu.cs.course1.tree;

import java.util.function.Function;

/**
 * Реализация простейшего бинарного дерева
 */
public class SimpleBinaryTree<T> implements BinaryTree<T> {

    protected class SimpleTreeNode implements BinaryTree.TreeNode<T> {
        public T value;
        public SimpleTreeNode left;
        public SimpleTreeNode right;

        public SimpleTreeNode(T value, SimpleTreeNode left, SimpleTreeNode right) {
            this.value = value;
            this.left = left;
            this.right = right;
        }

        public SimpleTreeNode(T value) {
            this(value, null, null);
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public TreeNode<T> getLeft() {
            return left;
        }

        @Override
        public TreeNode<T> getRight() {
            return right;
        }


        // Реализация консольного вывода дерева
        public void traverseNodes(StringBuilder sb, String padding, String pointer, TreeNode node,
                                  boolean hasRightSibling) {
            if (node != null) {
                sb.append("\n");
                sb.append(padding);
                sb.append(pointer);
                sb.append(node.getValue());

                StringBuilder paddingBuilder = new StringBuilder(padding);
                if (hasRightSibling) {
                    paddingBuilder.append("│  ");
                } else {
                    paddingBuilder.append("   ");
                }

                String paddingForBoth = paddingBuilder.toString();
                String pointerRight = "└──";
                String pointerLeft = (node.getRight() != null) ? "├──" : "└──";

                traverseNodes(sb, paddingForBoth, pointerLeft, node.getLeft(), node.getRight() != null);
                traverseNodes(sb, paddingForBoth, pointerRight, node.getRight(), false);
            }
        }

        public String traversePreOrder(TreeNode root) {

            if (root == null) {
                return "null";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(root.getValue());

            String pointerRight = "└──";
            String pointerLeft = (root.getRight() != null) ? "├──" : "└──";

            traverseNodes(sb, "", pointerLeft, root.getLeft(), root.getRight() != null);
            traverseNodes(sb, "", pointerRight, root.getRight(), false);

            return sb.toString();
        }
    }

    protected SimpleTreeNode root = null;

    protected Function<String, T> fromStrFunc;
    protected Function<T, String> toStrFunc;

    public SimpleBinaryTree(Function<String, T> fromStrFunc, Function<T, String> toStrFunc) {
        this.fromStrFunc = fromStrFunc;
        this.toStrFunc = toStrFunc;
    }

    public SimpleBinaryTree(Function<String, T> fromStrFunc) {
        this(fromStrFunc, Object::toString);
    }

    public SimpleBinaryTree() {
        this(null);
    }

    @Override
    public TreeNode<T> getRoot() {
        return root;
    }

    public void clear() {
        root = null;
    }

    private T fromStr(String s) throws Exception {
        s = s.trim();
        if (s.length() > 0 && s.charAt(0) == '"') {
            s = s.substring(1);
        }
        if (s.length() > 0 && s.charAt(s.length() - 1) == '"') {
            s = s.substring(0, s.length() - 1);
        }
        if (fromStrFunc == null) {
            throw new Exception("Не определена функция конвертации строки в T");
        }
        return fromStrFunc.apply(s);
    }

    private static class IndexWrapper {
        public int index = 0;
    }

    private void skipSpaces(String bracketStr, IndexWrapper iw) {
        while (iw.index < bracketStr.length() && Character.isWhitespace(bracketStr.charAt(iw.index))) {
            iw.index++;
        }
    }

    private T readValue(String bracketStr, IndexWrapper iw) throws Exception {
        // пропуcкаем возможные пробелы
        skipSpaces(bracketStr, iw);
        if (iw.index >= bracketStr.length()) {
            return null;
        }
        int from = iw.index;
        boolean quote = bracketStr.charAt(iw.index) == '"';
        if (quote) {
            iw.index++;
        }
        while (iw.index < bracketStr.length() && (
                    quote && bracketStr.charAt(iw.index) != '"' ||
                    !quote && !Character.isWhitespace(bracketStr.charAt(iw.index)) && "(),".indexOf(bracketStr.charAt(iw.index)) < 0
               )) {
            iw.index++;
        }
        if (quote && bracketStr.charAt(iw.index) == '"') {
            iw.index++;
        }
        String valueStr = bracketStr.substring(from, iw.index);
        T value = fromStr(valueStr);
        skipSpaces(bracketStr, iw);
        return value;
    }

    private SimpleTreeNode fromBracketStr(String bracketStr, IndexWrapper iw) throws Exception {
        T parentValue = readValue(bracketStr, iw);
        SimpleTreeNode parentNode = new SimpleTreeNode(parentValue);
        if (bracketStr.charAt(iw.index) == '(') {
            iw.index++;
            skipSpaces(bracketStr, iw);
            if (bracketStr.charAt(iw.index) != ',') {
                parentNode.left = fromBracketStr(bracketStr, iw);
                skipSpaces(bracketStr, iw);
            }
            if (bracketStr.charAt(iw.index) == ',') {
                iw.index++;
                skipSpaces(bracketStr, iw);
            }
            if (bracketStr.charAt(iw.index) != ')') {
                parentNode.right = fromBracketStr(bracketStr, iw);
                skipSpaces(bracketStr, iw);
            }
            if (bracketStr.charAt(iw.index) != ')') {
                throw new Exception(String.format("Ожидалось ')' [%d]", iw.index));
            }
            iw.index++;
        }

        return parentNode;
    }

    public void fromBracketNotation(String bracketStr) throws Exception {
        IndexWrapper iw = new IndexWrapper();
        SimpleTreeNode root = fromBracketStr(bracketStr, iw);
        if (iw.index < bracketStr.length()) {
            throw new Exception(String.format("Ожидался конец строки [%d]", iw.index));
        }
        this.root = root;
    }


    /*
    20.
    Написать метод клонирования дерева (создания дерева, в котором структура и все
    значения будут такими же, как в исходном дереве, но все объекты будут другими).
    */


    // Метод рекурсивно копирующий ноды (узлы)
    private SimpleTreeNode cloneBinaryTree(SimpleTreeNode root) {
        // Проверка корня дерева
        if (root == null) {
            return null;
        }

        // Создаём новый узел который имеет значение текущего узла
        SimpleTreeNode root_copy = new SimpleTreeNode(root.value);

        // Рекурсивно копируем левых и правых потомков
        root_copy.left = cloneBinaryTree(root.left);
        root_copy.right = cloneBinaryTree(root.right);

        // return cloned root node
        return root_copy;
    }

    // Метод для клонирования самого дерева
    public SimpleBinaryTree<T> clone(SimpleBinaryTree<T> tree) {
        tree.root = cloneBinaryTree(root);
        return tree;
    }

    // Метод выводящий дерево в консоль
    public void printBinaryTree(SimpleBinaryTree<T> tree) {
        System.out.println(tree.root.traversePreOrder(root));
    }

}
