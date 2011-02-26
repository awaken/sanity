package cri.sanity;


public final class CallFilter
{
	
	public boolean check(String id, String num)
	{
		if(!A.is("callfilter_enable_"+id)) return true;
		return true;
	}
	
	public void search(String num)
	{
		
	}
}
