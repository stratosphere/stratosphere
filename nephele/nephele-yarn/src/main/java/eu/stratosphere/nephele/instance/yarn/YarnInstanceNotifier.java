package eu.stratosphere.nephele.instance.yarn;

import java.util.List;

import eu.stratosphere.nephele.instance.AbstractInstance;
import eu.stratosphere.nephele.instance.AllocatedResource;
import eu.stratosphere.nephele.instance.InstanceListener;
import eu.stratosphere.nephele.jobgraph.JobID;

/**
 * This class is an auxiliary class to send the notification
 * about the availability of an {@link AbstractInstance} to the given {@link InstanceListener} object. The notification
 * must be sent from
 * a separate thread, otherwise the atomic operation of requesting an instance
 * for a vertex and switching to the state ASSINING could not be guaranteed.
 * This class is thread-safe.
 * 
 * @author warneke
 */
public final class YarnInstanceNotifier extends Thread {

	/**
	 * The {@link InstanceListener} object to send the notification to.
	 */
	private final InstanceListener instanceListener;

	/**
	 * The ID of the job the notification refers to.
	 */
	private final JobID jobID;

	/**
	 * The allocated resources the notification refers to.
	 */
	private final List<AllocatedResource> allocatedResources;

	/**
	 * Constructs a new instance notifier object.
	 * 
	 * @param instanceListener
	 *        the listener to send the notification to
	 * @param jobID
	 *        the ID of the job the notification refers to
	 * @param allocatedResources
	 *        the resources with has been allocated for the job
	 */
	public YarnInstanceNotifier(final InstanceListener instanceListener, final JobID jobID,
			final List<AllocatedResource> allocatedResources) {
		this.instanceListener = instanceListener;
		this.jobID = jobID;
		this.allocatedResources = allocatedResources;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {

		this.instanceListener.resourcesAllocated(this.jobID, this.allocatedResources);
	}
}