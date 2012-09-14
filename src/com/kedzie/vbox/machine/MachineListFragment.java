package com.kedzie.vbox.machine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.kedzie.vbox.BundleBuilder;
import com.kedzie.vbox.MetricPreferencesActivity;
import com.kedzie.vbox.PreferencesActivity;
import com.kedzie.vbox.R;
import com.kedzie.vbox.Utils;
import com.kedzie.vbox.VBoxApplication;
import com.kedzie.vbox.VMAction;
import com.kedzie.vbox.api.IConsole;
import com.kedzie.vbox.api.IMachine;
import com.kedzie.vbox.api.IManagedObjectRef;
import com.kedzie.vbox.api.IProgress;
import com.kedzie.vbox.api.jaxb.VBoxEventType;
import com.kedzie.vbox.metrics.MetricActivity;
import com.kedzie.vbox.soap.VBoxSvc;
import com.kedzie.vbox.task.ActionBarTask;
import com.kedzie.vbox.task.DialogTask;
import com.kedzie.vbox.task.LaunchVMProcessTask;
import com.kedzie.vbox.task.MachineTask;

public class MachineListFragment extends SherlockFragment {
	private static final int REQUEST_CODE_PREFERENCES = 6;
	protected static final String TAG = MachineListFragment.class.getSimpleName();
	
	private VBoxSvc _vmgr;
	private List<IMachine> _machines;
	private ListView _listView;
	/** Selected item index. -1 means it was not set in a saved state. */
	private int _curCheckPosition=-1;
	/** Whether the  details {@link Fragment} is displayed */
	private boolean _dualPane;
	/** Handles selection of a VM in the list */
	private SelectMachineListener _machineSelectedListener;
	private LocalBroadcastManager lbm;
	private BroadcastReceiver _receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(VBoxEventType.ON_MACHINE_STATE_CHANGED)) {
				Log.i(TAG, "Recieved Machine State Changed Event Broadcast");
				new HandleEventTask(_vmgr).execute(intent.getExtras());
			}
		}
	};
	
	/**
	 * Listener who is notified when a VirtualMachine is selected from the list
	 */
	public static interface SelectMachineListener {
		
		/**
		 * Virtual Machine has been selected from list
		 * @param machine The selected {@link IMachine}
		 */
		public void onMachineSelected(IMachine machine);
	}
	
	/** 
	 * Load the Machines 
	 */
	class LoadMachinesTask extends DialogTask<Void, List<IMachine>>	{
		public LoadMachinesTask(VBoxSvc vmgr) { 
			super( "LoadMachinesTask", getActivity(), vmgr, "Loading Machines");
		}

		@Override
		protected List<IMachine> work(Void... params) throws Exception {
			List<IMachine> machines =_vmgr.getVBox().getMachines(); 
			_vmgr.getVBox().getHost().getMemorySize();
			for(IMachine m :  machines) {
				//cache property values to avoid remote calls
				m.getState(); m.getName();  m.getOSTypeId(); m.getCurrentStateModified(); 
				if(m.getCurrentSnapshot()!=null) 
					m.getCurrentSnapshot().getName();
			}
			_vmgr.getVBox().getPerformanceCollector().setupMetrics(new String[] { "*:" }, 
					Utils.getIntPreference(getActivity(), MetricPreferencesActivity.PERIOD), 
					Utils.getIntPreference(getActivity(), MetricPreferencesActivity.COUNT), 
					(IManagedObjectRef)null);
			_vmgr.getVBox().getVersion();
			return machines;
		}

		@Override
		protected void onResult(List<IMachine> result)	{
			_machines = result;
			_listView.setAdapter(new MachineListAdapter(result));
			getAdapter().setNotifyOnChange(false);
		}
	}
	
	/**
	 * Handle MachineStateChanged event
	 */
	private class HandleEventTask extends ActionBarTask<Bundle, IMachine> {
		
		public HandleEventTask(VBoxSvc vmgr) { 
			super( "HandleEventTask", getSherlockActivity(), vmgr);
		}

		@Override
		protected IMachine work(Bundle... params) throws Exception {
			IMachine m = BundleBuilder.getProxy(params[0], IMachine.BUNDLE, IMachine.class);
			MachineView.cacheProperties(m);
			return m;
		}

		@Override
		protected void onResult(IMachine result)	{
				int pos = getAdapter().getPosition(result);
				getAdapter().setNotifyOnChange(false);
				getAdapter().remove(result);
				getAdapter().insert(result, pos);
				getAdapter().notifyDataSetChanged();
				Utils.toastShort(getActivity(), "%s changed State: [%s]", result.getName(), result.getState());
		}
	}
	
	/**
	 * List adapter for Virtual Machines
	 */
	class MachineListAdapter extends ArrayAdapter<IMachine> {
		public MachineListAdapter(List<IMachine> machines) {
			super(getActivity(), 0, machines);
		}
		public View getView(int position, View view, ViewGroup parent) {
			if (view == null) view = new MachineView(getApp(), getActivity());
			((MachineView)view).update(getItem(position));
			return view;
		}
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		_machineSelectedListener = (SelectMachineListener)activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		_listView = new ListView(getActivity());
//		registerForContextMenu(_listView);
		_listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				getSherlockActivity().startActionMode(new ActionCallback(getAdapter().getItem(position)));
				return true;
			}
		});
       	_listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				showDetails(position);
			}
		});
       	return _listView;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		_vmgr = (VBoxSvc)getActivity().getIntent().getParcelableExtra(VBoxSvc.BUNDLE);
		lbm = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
		setHasOptionsMenu(true);
		
		View detailsFrame = getActivity().findViewById(R.id.details);
		_dualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
		if(_dualPane)
			_listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		if(savedInstanceState!=null)  { 
    		_curCheckPosition = savedInstanceState.getInt("curChoice", -1);
    		_machines = (ArrayList<IMachine>)savedInstanceState.getSerializable("machines");
    		_listView.setAdapter(new MachineListAdapter(_machines));
    		if(_curCheckPosition>-1)
    			showDetails(_curCheckPosition);
    	} else
    		new LoadMachinesTask(_vmgr).execute();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		lbm.registerReceiver(_receiver, new IntentFilter(VBoxEventType.ON_MACHINE_STATE_CHANGED.name()));
	}

	@Override
	public void onStop() {
		super.onStop();
		lbm.unregisterReceiver(_receiver);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("curChoice", _curCheckPosition);
		outState.putSerializable("machines", (Serializable) _machines);
	}
	
	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
		inflater.inflate(R.menu.machine_list_actions, menu);
	}

	@Override
	public boolean onOptionsItemSelected( com.actionbarsherlock.view.MenuItem item) {
		switch(item.getItemId()) {
		case R.id.option_menu_refresh:
			new LoadMachinesTask(_vmgr).execute();
			return true;
		case R.id.machine_list_option_menu_metrics:
			startActivity(new Intent(getActivity(), MetricActivity.class).putExtra(VBoxSvc.BUNDLE, _vmgr)
					.putExtra(MetricActivity.INTENT_TITLE, R.string.host_metrics)
					.putExtra(MetricActivity.INTENT_OBJECT, _vmgr.getVBox().getHost().getIdRef() )
					.putExtra(MetricActivity.INTENT_RAM_AVAILABLE, _vmgr.getVBox().getHost().getMemorySize())
					.putExtra(MetricActivity.INTENT_CPU_METRICS , new String[] { "CPU/Load/User", "CPU/Load/Kernel" } )
					.putExtra(MetricActivity.INTENT_RAM_METRICS , new String[] {  "RAM/Usage/Used" }));
			return true;
		case R.id.option_menu_preferences:
			startActivityForResult(new Intent(getActivity(), PreferencesActivity.class),REQUEST_CODE_PREFERENCES);
			return true;
		default:
			return true;
		}
	}

	@SuppressWarnings("unchecked")
	protected ArrayAdapter<IMachine> getAdapter() {
		return (ArrayAdapter<IMachine>)_listView.getAdapter();
	}

	public VBoxApplication getApp() { 
		return (VBoxApplication)getActivity().getApplication(); 
	}
	
	void showDetails(int index) {
        _curCheckPosition = index;
        if (_dualPane)
        	_listView.setItemChecked(index, true);
       	_machineSelectedListener.onMachineSelected(getAdapter().getItem(index));
    }
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle(R.string.machine_context_title);
		IMachine m = getAdapter().getItem(((AdapterContextMenuInfo)menuInfo).position);
		List<VMAction> actions = Arrays.asList(VMAction.getVMActions(m.getState()));
		if(actions.contains(VMAction.START))
			menu.add(Menu.NONE, R.id.machines_context_menu_start, Menu.NONE, VMAction.START.toString());
		if(actions.contains(VMAction.POWER_OFF))
			menu.add(Menu.NONE, R.id.machines_context_menu_poweroff, Menu.NONE, VMAction.POWER_OFF.toString());
		if(actions.contains(VMAction.POWER_BUTTON))	
			menu.add(Menu.NONE, R.id.machines_context_menu_acpi, Menu.NONE, VMAction.POWER_BUTTON.toString());
		if(actions.contains(VMAction.RESET))
			menu.add(Menu.NONE, R.id.machines_context_menu_reset, Menu.NONE, VMAction.RESET.toString());
		if(actions.contains(VMAction.PAUSE))
			menu.add(Menu.NONE, R.id.machines_context_menu_pause, Menu.NONE, VMAction.PAUSE.toString());
		if(actions.contains(VMAction.RESUME))
			menu.add(Menu.NONE, R.id.machines_context_menu_resume, Menu.NONE, VMAction.RESUME.toString());
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
	  IMachine m = getAdapter().getItem( ((AdapterContextMenuInfo) item.getMenuInfo()).position);
	  switch (item. getItemId()) {
	  case R.id.machines_context_menu_start:  
		  new LaunchVMProcessTask(getActivity().getApplicationContext(), _vmgr).execute(m);	  
		  break;
	  case R.id.machines_context_menu_poweroff:   
		  new MachineTask<IMachine>("PoweroffTask", getActivity(), _vmgr, "Powering Off", false, m) {	
			  protected IProgress workWithProgress(IMachine m,  IConsole console, IMachine...i) throws Exception { 	
				  return console.powerDown();
			  }
		  }.execute(m);
		  break;
	  case R.id.machines_context_menu_reset:	 
		  new MachineTask<IMachine>("ResetTask", getActivity(), _vmgr, "Resetting", true, m) {	
			  protected void work(IMachine m,  IConsole console, IMachine...i) throws Exception { 	
				  console.reset(); 
			  }
			  }.execute(m);
		  break;
	  case R.id.machines_context_menu_resume:	  
		  new MachineTask<IMachine>("ResumeTask", getActivity(), _vmgr, "Resuming", true, m) {	
			  protected void work(IMachine m,  IConsole console, IMachine...i) throws Exception { 	
				  console.resume(); 
			  }
		  }.execute(m);
		  break;
	  case R.id.machines_context_menu_pause:	  
		  new MachineTask<IMachine>("PauseTask", getActivity(), _vmgr, "Pausing", true, m) {	
			  protected void work(IMachine m,  IConsole console, IMachine...i) throws Exception {  
				  console.pause();	
			  }
		  }.execute(m);
		  break;
	  case R.id.machines_context_menu_acpi:	  
		  new MachineTask<IMachine>("ACPITask", getActivity(), _vmgr, "ACPI Power Down", true, m) {
			  protected void work(IMachine m,  IConsole console,IMachine...i) throws Exception {	
				  console.powerButton(); 	
			  }
		  }.execute(m);
		  break;
	  }
	  return true;
	}
	
	class ActionCallback implements ActionMode.Callback {
		private IMachine _machine;
		
		public ActionCallback(IMachine machine) {
			_machine = machine;
		}
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, com.actionbarsherlock.view.Menu menu) {
			mode.getMenuInflater().inflate(R.menu.machine_list_context, menu);
			return true;
		}
		
		@Override
		public boolean onPrepareActionMode(ActionMode mode, com.actionbarsherlock.view.Menu menu) {
			return false;
		}
		
		@Override
		public boolean onActionItemClicked(ActionMode mode, com.actionbarsherlock.view.MenuItem item) {
			return false;
		}
		
		@Override
		public void onDestroyActionMode(ActionMode mode) {
		}
	}
}
