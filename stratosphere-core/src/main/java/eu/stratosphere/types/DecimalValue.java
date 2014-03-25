package eu.stratosphere.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class DecimalValue implements Value {
	private static final long serialVersionUID = 1L;
	
	private BigDecimal value;
	private byte[] landingZone;
	
	public DecimalValue() {
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		final byte[] bytes = value.unscaledValue().toByteArray();
		out.writeInt(bytes.length);
		out.writeInt(value.scale());
		out.write(bytes);
	}

	@Override
	public void read(DataInput in) throws IOException {
		final int len = in.readInt();
		final int scale = in.readInt();
		if(landingZone == null || landingZone.length != len) {
			// try to avoid at least some serialization.
			landingZone = new byte[len];
		}
		in.readFully(landingZone);
		value = new BigDecimal(new BigInteger(landingZone), scale);
	}
	
	public BigDecimal getValue() {
		return value;
	}
	
	public void setValue(BigDecimal d) {
		this.value = d;
	}
	
	
}
