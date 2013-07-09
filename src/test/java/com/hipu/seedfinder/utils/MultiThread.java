package com.hipu.seedfinder.utils;

public class MultiThread {
	
	private  ThreadLocal<Integer> count = new ThreadLocal<Integer>() {
		public Integer initialValue() {
			return 1;
		}
	};
	
	public MultiThread() {
	}
	
	public  void addCount() {
		count.set(count.get()+1);
	}
	
	public void showCount() {
		System.out.println(count.get());
	}
	
	public synchronized void show() {
		System.out.println("ok");
	}
	
	public synchronized void waiting() {
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		MultiThread t = new MultiThread();
		WaitJob w = new WaitJob(t);
		Thread t1 = new Thread(w);
		
		ShowJob s = new ShowJob(t);
		Thread t2 = new Thread(s);
		
		t1.start();
		t2.start();
	}
}

class WaitJob implements Runnable{
	
	MultiThread thread;
	
	public WaitJob(MultiThread t) {
		this.thread = t;
	}

	@Override
	public void run() {
		thread.waiting();
		thread.addCount();
		thread.showCount();
	}
}

class ShowJob implements Runnable{
	
	MultiThread thread;
	
	public ShowJob(MultiThread t){
		this.thread = t;
	}

	@Override
	public void run() {
		thread.show();
		thread.addCount();
		thread.showCount();
	}
}
