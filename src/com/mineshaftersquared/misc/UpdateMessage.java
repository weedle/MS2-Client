package com.mineshaftersquared.misc;

import com.creatifcubed.simpleapi.SimpleVersion;

public class UpdateMessage {
	public int messageId;
	public SimpleVersion left;
	public SimpleVersion right;
	public BoundType leftBound;
	public BoundType rightBound;
	public MessageType messageType;
	public String message;
	
	public UpdateMessage() {
		this.left = null;
		this.right = null;
		this.leftBound = null;
		this.right = null;
		this.messageType = null;
		this.message = null;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public MessageType getMessageType() {
		return this.messageType;
	}
	
	public int getMessageId() {
		return this.messageId;
	}
	
	@Override
	public int hashCode() {
		return this.messageId;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof UpdateMessage) {
			UpdateMessage that = (UpdateMessage) other;
			return this.messageId == that.messageId;
		}
		return false;
	}
	
	public boolean appliesTo(SimpleVersion version) {
		BoundType leftBound = this.leftBound;
		if (this.left == null || this.leftBound == null) {
			leftBound = BoundType.INFINITE;
		}
		BoundType rightBound = this.rightBound;
		if (this.right == null || this.rightBound == null) {
			rightBound = BoundType.INFINITE;
		}
		return leftBound.isValInBound(version, this.left, BoundType.Side.LEFT) && rightBound.isValInBound(version, this.right, BoundType.Side.RIGHT);
	}
	
	public static enum BoundType {
		OPEN,
		CLOSE,
		INFINITE;
		
		public <T> boolean isValInBound(Comparable<T> val, T bound, Side side) {
			if (this == INFINITE) {
				return true;
			}
			int compare = val.compareTo(bound);
			if (compare == 0 && this == CLOSE) {
				return true;
			}
			if (side == Side.LEFT && compare > 0) {
				return true;
			}
			if (side == Side.RIGHT && compare < 0) {
				return true;
			}
			return false;
		}
		
		public static enum Side {
			LEFT,
			RIGHT;
		}
	}
	
	public static enum MessageType {
		SHOW_ONCE,
		SHOW_ALWAYS,
		MUST_UPDATE;
	}
}
