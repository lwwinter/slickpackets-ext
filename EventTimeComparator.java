//package org.timecrunch;

import java.util.Comparator;

public class EventTimeComparator implements Comparator<SimEvent>
{
    @Override
    public int compare(SimEvent e1, SimEvent e2)
    {
        if (e1.mTriggerTime < e2.mTriggerTime)
        {
            return -1;
        }
        if (e1.mTriggerTime > e2.mTriggerTime)
        {
            return 1;
        }
        return 0;
    }
}
