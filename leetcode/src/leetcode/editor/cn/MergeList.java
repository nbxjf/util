package leetcode.editor.cn;

import java.util.Arrays;

import leetcode.editor.cn.AddTwoNumbers.ListNode;

/**
 * Created by Jeff_xu on 2021/3/2.
 *
 * @author Jeff_xu
 */
public class MergeList {

    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        if (l1 == null && l2 == null) {
            return null;
        }
        if (l1 == null) {
            return l2;
        }
        if (l2 == null) {
            return l1;
        }

        ListNode head = null;
        if (l1.val <= l2.val) {
            head = l1;
            l1 = l1.next;
        } else {
            head = l2;
            l2 = l2.next;
        }

        ListNode l3 = head;
        while (l1 != null && l2 != null) {
            if (l1.val <= l2.val) {
                l3.next = l1;
                l1 = l1.next;
            } else {
                l3.next = l2;
                l2 = l2.next;
            }
            l3 = l3.next;
        }
        if (l1 != null) {
            while (l1 != null) {
                l3.next = l1;
                l1 = l1.next;
                l3 = l3.next;

            }
        }
        if (l2 != null) {
            while (l2 != null) {
                l3.next = l2;
                l2 = l2.next;
                l3 = l3.next;

            }
        }
        return head;
    }

    //public static void main(String[] args) {
    //    MergeList mergeList = new MergeList();
    //    ListNode listNode = mergeList.mergeTwoLists(new ListNode(1, new ListNode(2, new ListNode(4))), new ListNode(1, new ListNode(2, new ListNode(4))));
    //    System.out.println(listNode);
    //
    //}

    public static void main(String[] args) {
        int[] ints = Arrays.copyOfRange(new int[] {1, 2, 3, 4, 5, 6}, 0, 1);
        for (int anInt : ints) {
            System.out.println(anInt);
        }
    }
}
