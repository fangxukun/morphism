package com.morphism.search.ann;

/**
 * User: xukun.fyp
 * Date: 17/3/12
 * Time: 16:41
 */
public class Item {
	protected long 				id;
	protected float[]			vector;

	public Item(long id, float[] vector) {
		this.id = id;
		this.vector = vector;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Item))
			return false;

		Item item = (Item) o;

		return id == item.id;

	}

	public long getId() {
		return id;
	}

	public float[] getVector() {
		return vector;
	}

	@Override
	public int hashCode() {
		return (int) (id ^ (id >>> 32));
	}
}
