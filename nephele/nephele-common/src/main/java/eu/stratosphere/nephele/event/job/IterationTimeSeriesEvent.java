package eu.stratosphere.nephele.event.job;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import eu.stratosphere.nephele.event.job.AbstractEvent;
import eu.stratosphere.nephele.types.StringRecord;

public class IterationTimeSeriesEvent extends AbstractEvent {
    
    private String seriesName;
    
    private int timeStep;
    
    private double value;
    
    
    
    public IterationTimeSeriesEvent(long timestamp, String seriesName,
            int timeStep, double value) {
        super(timestamp);
        this.seriesName = seriesName;
        this.timeStep = timeStep;
        this.value = value;
    }
    
    /**
     * Constructs a new IterationTimeSeriesEvent object. This constructor
     * is required for the deserialization process and is not
     * supposed to be called directly.
     */
    public IterationTimeSeriesEvent() {
        super();
        this.seriesName = null;
        this.timeStep = -1;
        this.value = -1;
    }
    
    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public int getTimeStep() {
        return timeStep;
    }

    public void setTimeStep(int timeStep) {
        this.timeStep = timeStep;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void read(final DataInput in) throws IOException {
        super.read(in);

        this.seriesName = StringRecord.readString(in);
        this.timeStep =in.readInt();
        this.value = in.readDouble();
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final DataOutput out) throws IOException {
        super.write(out);

        StringRecord.writeString(out, this.seriesName);
        out.writeInt(this.timeStep);
        out.writeDouble(this.value);
    }

    
    /**
     * {@inheritDoc}
     */
    public String toString() {

        return timestampToString(getTimestamp()) + "\t" + this.seriesName + " " + this.timeStep + " " + this.value;
    }


}
