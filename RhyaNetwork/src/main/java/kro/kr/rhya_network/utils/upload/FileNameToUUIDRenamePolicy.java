package kro.kr.rhya_network.utils.upload;

import java.io.File;
import java.util.UUID;

import com.oreilly.servlet.multipart.FileRenamePolicy;

public class FileNameToUUIDRenamePolicy implements FileRenamePolicy {
	private String uuid = null;
	public int imageFile = -1;
	public int mp3File = -1;
	
	public FileNameToUUIDRenamePolicy() {
		UUID uuid = UUID.randomUUID();
		this.uuid = uuid.toString();
	}
	
	
	public String getUUID() {
		return this.uuid;
	}
	
	
	public File rename(File f) {
		int type = 0;
		
		String fileName = f.getName();
		String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
		if (ext.equals("mp3")) {
			type = 0;
		}else {
			type = 1;
		}
		
		switch (type) {
			case 0: {
				f = new File(f.getParent(), uuid.concat(".mp3"));
				mp3File = 0;
				break;
			}
			
			case 1: {
				f = new File(f.getParent(), uuid.concat(".png"));
				imageFile = 0;
				break;
			}
		}
		return f;
	}
}
