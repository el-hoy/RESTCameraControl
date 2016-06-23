
package org.restcameracontrol.beans;

public class SupportedFolderOperations {

	private boolean deleteAll;
	private boolean putFile;
	private boolean makeDir;
	private boolean removeDir;

	public boolean isDeleteAll() {
		return deleteAll;
	}

	public void setDeleteAll(boolean deleteAll) {
		this.deleteAll = deleteAll;
	}

	public boolean isPutFile() {
		return putFile;
	}

	public void setPutFile(boolean putFile) {
		this.putFile = putFile;
	}

	public boolean isMakeDir() {
		return makeDir;
	}

	public void setMakeDir(boolean makeDir) {
		this.makeDir = makeDir;
	}

	public boolean isRemoveDir() {
		return removeDir;
	}

	public void setRemoveDir(boolean removeDir) {
		this.removeDir = removeDir;
	}

}
