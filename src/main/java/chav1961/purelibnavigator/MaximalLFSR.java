package chav1961.purelibnavigator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MaximalLFSR {
    private int GetFeedbackSize(int v)
    {
        int r = 0;

        while ((v >>= 1) != 0)
        {
          r++;
        }
        if (r < 4)
            r = 4;
        return (int)r;
    }
    // https://coderoad.ru/1816534/%D0%90%D0%BB%D0%B3%D0%BE%D1%80%D0%B8%D1%82%D0%BC-%D1%81%D0%BB%D1%83%D1%87%D0%B0%D0%B9%D0%BD%D0%BE%D0%B3%D0%BE-%D0%B2%D0%BE%D1%81%D0%BF%D1%80%D0%BE%D0%B8%D0%B7%D0%B2%D0%B5%D0%B4%D0%B5%D0%BD%D0%B8%D1%8F
    
    static int[] _feedback = new int[] {
        0x9, 0x17, 0x30, 0x44, 0x8e,
        0x108, 0x20d, 0x402, 0x829, 0x1013, 0x203d, 0x4001, 0x801f,
        0x1002a, 0x2018b, 0x400e3, 0x801e1, 0x10011e, 0x2002cc, 0x400079, 0x80035e,
        0x1000160, 0x20001e4, 0x4000203, 0x8000100, 0x10000235, 0x2000027d, 0x4000016f, 0x80000478
    };

    private int GetFeedbackTerm(int bits)
    {
        if (bits < 4 || bits >= 28)
            throw new IllegalArgumentException("bits");
        return _feedback[bits];
    }

    public Iterable<Integer> RandomIndexes(int count) {	
    	final List<Integer>	result = new ArrayList<>(); 
    
        if (count < 0)
            throw new IllegalArgumentException("count");

        int bitsForFeedback = GetFeedbackSize(count);

        Random r = new Random();
        int i = r.nextInt(count - 1);

        int feedback = GetFeedbackTerm(bitsForFeedback);
        int valuesReturned = 0;
        while (valuesReturned < count)
        {
            if ((i & 1) != 0)
            {
                i = (i >> 1) ^ feedback;
            }
            else {
                i = (i >> 1);
            }
            if (i <= count)
            {
                valuesReturned++;
                result.add(i-1);
            }
        }
        return result;
    }
    
    public static void main(String[] args) {
         MaximalLFSR lfsr = new MaximalLFSR();
                for(int i : lfsr.RandomIndexes(100))
                {
                    System.err.println(i);
                }
    }
    
}