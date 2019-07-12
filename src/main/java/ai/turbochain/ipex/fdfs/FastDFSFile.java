package ai.turbochain.ipex.fdfs;

import lombok.Data;

@Data
public class FastDFSFile {

	public FastDFSFile(String name, byte[] content, String ext) {
		this.name=name;
		this.content=content;
		this.ext=ext;
	}

	private String name;

    private byte[] content;

    private String ext;

    private String md5;

    private String author;
}
