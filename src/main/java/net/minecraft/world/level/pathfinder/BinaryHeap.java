package net.minecraft.world.level.pathfinder;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/pathfinder/BinaryHeap.class */
public class BinaryHeap {
    private Node[] heap = new Node[128];
    private int size;

    public Node insert(Node node) {
        if (node.heapIdx >= 0) {
            throw new IllegalStateException("OW KNOWS!");
        }
        if (this.size == this.heap.length) {
            Node[] nodeArr = new Node[this.size << 1];
            System.arraycopy(this.heap, 0, nodeArr, 0, this.size);
            this.heap = nodeArr;
        }
        this.heap[this.size] = node;
        node.heapIdx = this.size;
        int i = this.size;
        this.size = i + 1;
        upHeap(i);
        return node;
    }

    public void clear() {
        this.size = 0;
    }

    public Node pop() {
        Node node = this.heap[0];
        Node[] nodeArr = this.heap;
        Node[] nodeArr2 = this.heap;
        int i = this.size - 1;
        this.size = i;
        nodeArr[0] = nodeArr2[i];
        this.heap[this.size] = null;
        if (this.size > 0) {
            downHeap(0);
        }
        node.heapIdx = -1;
        return node;
    }

    public void changeCost(Node node, float f) {
        float f2 = node.f;
        node.f = f;
        if (f < f2) {
            upHeap(node.heapIdx);
        } else {
            downHeap(node.heapIdx);
        }
    }

    private void upHeap(int i) {
        Node node = this.heap[i];
        float f = node.f;
        while (i > 0) {
            int i2 = (i - 1) >> 1;
            Node node2 = this.heap[i2];
            if (f >= node2.f) {
                break;
            }
            this.heap[i] = node2;
            node2.heapIdx = i;
            i = i2;
        }
        this.heap[i] = node;
        node.heapIdx = i;
    }

    private void downHeap(int i) {
        Node node;
        float f;
        Node node2 = this.heap[i];
        float f2 = node2.f;
        while (true) {
            int i2 = 1 + (i << 1);
            int i3 = i2 + 1;
            if (i2 < this.size) {
                Node node3 = this.heap[i2];
                float f3 = node3.f;
                if (i3 >= this.size) {
                    node = null;
                    f = Float.POSITIVE_INFINITY;
                } else {
                    node = this.heap[i3];
                    f = node.f;
                }
                if (f3 >= f) {
                    if (f >= f2) {
                        break;
                    }
                    this.heap[i] = node;
                    node.heapIdx = i;
                    i = i3;
                } else {
                    if (f3 >= f2) {
                        break;
                    }
                    this.heap[i] = node3;
                    node3.heapIdx = i;
                    i = i2;
                }
            } else {
                break;
            }
        }
        this.heap[i] = node2;
        node2.heapIdx = i;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }
}
