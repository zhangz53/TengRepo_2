package com.teng.filter;

import java.util.ArrayList;

public class IIR {
	
	public ArrayList<Double> binomial_mult(int n, ArrayList<Double> p)
	{
		int i, j;
		ArrayList<Double> a = new ArrayList<Double>();
		
		//2*n size
		for(int itr =0; itr < (2*n); itr++)
		{
			a.add(0.0);
		}
		
		for(i = 0; i < n; ++i)
		{
			for(j = i; j > 0; --j)
			{
				a.set(2*j, a.get(2*j) + p.get(2*i)*a.get(2*(j-1)) - p.get(2*i + 1)*a.get(2*(j-1)+1));
				a.set(2*j+1, a.get(2*j+1) + p.get(2*i)*a.get(2*(j-1)+1) + p.get(2*i+1)*a.get(2*(j-1)));
			}
			a.set(0, a.get(0) + p.get(2*i));
			a.set(1, a.get(1) + p.get(2*i+1));
		}
		
		return a;
	}
	
	/*
	 * calculates the d coefficients for a butterworth lowpass 
	 */
	public ArrayList<Double> dcof_bwlp(int n, double fcf)
	{
		int k;            // loop variables
	    double theta;     // M_PI * fcf / 2.0
	    double st;        // sine of theta
	    double ct;        // cosine of theta
	    double parg;      // pole angle
	    double sparg;     // sine of the pole angle
	    double cparg;     // cosine of the pole angle
	    double a;         // workspace variable
	    ArrayList<Double> rcof = new ArrayList<Double>();
	    ArrayList<Double> dcof;
	    
	    //size to be 2*n
	    for(int itrr = 0; itrr < (2*n); itrr++)
	    {
	    	rcof.add(0.0);
	    }
	    
	    theta = Math.PI * fcf;
	    st = Math.sin(theta);
	    ct = Math.cos(theta);
	    
	    for(k = 0; k < n; ++k)
	    {
	    	parg = Math.PI * (double)(2*k + 1)/ (double)(2 * n);
	    	sparg = Math.sin(parg);
	    	cparg = Math.cos(parg);
	    	a = 1.0 + st * sparg;
	    	
	    	rcof.set(2*k, -ct/a);
	    	rcof.set(2*k+1,	-st*cparg/a);
	    }
	    
	    dcof = new ArrayList<Double>(binomial_mult(n, rcof));
	    
	    dcof.set(1, dcof.get(0));
	    dcof.set(0, 1.0);
	    for(k = 3; k <= n; ++k)
	    {
	    	dcof.set(k, dcof.get(2*k-2));
	    }
	    
	    return dcof;
	}
	
	
	/*
	 * calculates the d coefficients for a butterworth highpass 
  	filter*/
	public ArrayList<Double> dcof_bwhp(int n, double fcf)
	{
		return dcof_bwlp(n, fcf);
	}
	
	/*
	 * calculates the c coefficients for a butterworth lowpass 
  filter
	 */
	public ArrayList<Integer> ccof_bwlp(int n)
	{
		ArrayList<Integer> cof = new ArrayList();
		int m;
		
		//arrayList size to be n+1
		for(int itrc = 0; itrc < (n + 1); itrc++)
		{
			cof.add(0);
		}
		
		cof.set(0, 1);
		cof.set(1, n);
		m = n/2;
		for(int itr = 2; itr <= m; ++itr)
		{
			cof.set(itr, (n-itr+1)*cof.get(itr-1)/itr );
			cof.set(n-itr, cof.get(itr));
		}
		cof.set(n-1, n);
		cof.set(n, 1);
		
		return cof;
	}
	
	/* calculates the c coefficients for a butterworth highpass filter*/
	public ArrayList<Integer> ccof_bwhp(int n)
	{
		ArrayList<Integer> cof = new ArrayList(ccof_bwlp(n));
		
		for(int itr = 0; itr <= n; ++itr)
		{
			if(itr%2 == 0)
				cof.set(itr, -cof.get(itr));
		}
		
		return cof;
	}
	
	
	public double sf_bwhp(int n, double fcf)  //problem
	{
		int m, k;         // loop variables
	    double omega;     // M_PI * fcf
	    double fomega;    // function of omega
	    double parg0;     // zeroth pole angle
	    double sf;        // scaling factor
	    
	    omega = Math.PI * fcf;
	    fomega = Math.sin(omega);
	    parg0 = Math.PI/ (double)(2*n);
	    
	    m = n / 2;
	    sf = 1.0;
	    
	    for( k = 0; k < n/2; ++k )
	    {
	    	sf *= (1.0 + fomega * Math.sin((double)(2*k+1)*parg0));
	    }
	    
	    fomega = Math.cos(omega / 2.0);
	    if( n % 2  == 0)
	    	sf *= (fomega + Math.sin(omega / 2.0));
	    sf = Math.pow( fomega, n ) / sf;

	    return sf;
	}
}
