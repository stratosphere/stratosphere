package eu.stratosphere.runtime.io.serialization.types;

public enum SerializationTestTypeFactory {
	BOOLEAN(new BooleanType()),
	BYTE_ARRAY(new ByteArrayType()),
	BYTE_SUB_ARRAY(new ByteSubArrayType()),
	BYTE(new ByteType()),
	CHAR(new CharType()),
	DOUBLE(new DoubleType()),
	FLOAT(new FloatType()),
	INT(new IntType()),
	LONG(new LongType()),
	SHORT(new ShortType()),
	UNSIGNED_BYTE(new UnsignedByteType()),
	UNSIGNED_SHORT(new UnsignedShortType()),
	STRING(new AsciiStringType());

	private final SerializationTestType factory;

	SerializationTestTypeFactory(SerializationTestType type) {
		this.factory = type;
	}

	public SerializationTestType factory() {
		return this.factory;
	}
}
