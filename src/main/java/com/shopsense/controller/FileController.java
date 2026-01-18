package com.shopsense.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.shopsense.service.FileService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin(origins = "*") // Allow all origins for mobile app
public class FileController {

	@Autowired
	FileService fileService;
	
	@PostMapping("/upload")
	public HashMap<String, String> fileUpload(@RequestParam("file") MultipartFile file) throws IOException {
		HashMap<String, String> m = new HashMap<>();
		String url = fileService.saveFile(file);
		m.put("status", "success");
		m.put("fileUrl", url);
		return m;
	}

	@GetMapping(value = "uploads/{fileName:.+}", produces = MediaType.ALL_VALUE)
	public void fileDownload(@PathVariable("fileName") String fileName, HttpServletResponse response)
			throws IOException {
		try {
			InputStream is = fileService.getFile(fileName);
			
			// Set proper content type based on file extension
			String contentType = getContentType(fileName);
			response.setContentType(contentType);
			response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
			response.setHeader("ngrok-skip-browser-warning", "true");
			response.setHeader("Cache-Control", "public, max-age=31536000");
			
			StreamUtils.copy(is, response.getOutputStream());
			response.getOutputStream().flush();
		} catch (FileNotFoundException e) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.setContentType(MediaType.TEXT_PLAIN_VALUE);
			response.getWriter().write("File not found: " + fileName);
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.setContentType(MediaType.TEXT_PLAIN_VALUE);
			response.getWriter().write("Error reading file: " + e.getMessage());
		}
	}
	
	private String getContentType(String fileName) {
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
		switch (extension) {
			case "jpg":
			case "jpeg":
				return "image/jpeg";
			case "png":
				return "image/png";
			case "gif":
				return "image/gif";
			case "webp":
				return "image/webp";
			case "pdf":
				return "application/pdf";
			default:
				return MediaType.APPLICATION_OCTET_STREAM_VALUE;
		}
	}
}
