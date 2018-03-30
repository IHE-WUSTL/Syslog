/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wustl.mir.erl.IHETools.SyslogSender.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.model.SelectItem;

import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.wustl.mir.erl.IHETools.Util.Util;

/**
 *
 * @author rmoult01
 */
@ManagedBean(eager=true)
@ApplicationScoped
public class ApplicationBean implements Serializable {
	static final long serialVersionUID = 1L;
	
	private static final String fs = File.separator;

	private Logger log;
	private static final String APPLICATION_NAME = "SyslogSender";
	private static Path runSyslogSender = null;
	private HierarchicalINIConfiguration iniSyslogSender;

	public boolean isWiki() {
		return !Util.getWiki().isEmpty();
	}
	public String getWikiURL() {
		return Util.getWiki();
	}

	private SelectItem[] xmitTypes = {
			new SelectItem("UDP", "UDP"),
			new SelectItem("TCP", "TCP"),
			new SelectItem("TLS", "TLS")
	};

	private SelectItem[] certificateNames;

	private Path sampleMessageDirectory;

	private SelectItem[] sampleMessages = null;

	/** 
	 * Creates a new instance of ApplicationBean
	 * @throws Exception
	 */
	public ApplicationBean() throws Exception {

		try {

			Util.initializeApplication(APPLICATION_NAME);
			log = Util.getLog();

			//-------------------------------- locate ATNA SyslogSender installation
			String atnaRoot = Util.getParameterString("ATNA Syslog", 
					"RunDirectory", "/opt/syslog-*/run/SyslogSender");
			File[] fls = 
				new com.esotericsoftware.wildcard.Paths(fs, atnaRoot.substring(1))
					.dirsOnly().getFiles().toArray(new File[0]);
			if (fls.length == 0) throw new Exception(atnaRoot + " no path found");
			if (fls.length > 1) throw new Exception (atnaRoot + " multiple paths found");
			runSyslogSender = fls[0].toPath();

			//---------------------------- Load ATNA SyslogSender .ini file
			Path path = runSyslogSender.resolve("SyslogSender.ini");
			Util.isValidPfn("properties file", path, false, "rw");
			iniSyslogSender = new HierarchicalINIConfiguration();
			iniSyslogSender.setDelimiterParsingDisabled(true);
			iniSyslogSender.load(path.toFile());
			log.debug(path.getFileName() + " loaded.");

			//------------------------ load certificate names
			String[] cNames = Util.getParameterStringArray(null, "CertificateNames",
					iniSyslogSender);
			if (cNames == null || cNames.length == 0)
				throw new Exception("CertificateNames entry in .ini file invalid");
			certificateNames = new SelectItem[cNames.length];
			for (int i = 0; i < cNames.length; i++) {
				certificateNames[i] = new SelectItem(cNames[i], cNames[i]);
			}

			//------- load, validate sample messages directory
			sampleMessageDirectory = 
					runSyslogSender.resolve(Util.getParameterString(null, 
							"SampleMessageDirectory", "tm", iniSyslogSender));
			Util.isValidPfn("SyslogSender sample msg dir" , sampleMessageDirectory, 
					true, "r");

			//----------------- load sample messages from directory
			List<SelectItem> items = new ArrayList<SelectItem>();
			File[] files = new com.esotericsoftware.wildcard.Paths(
				sampleMessageDirectory.toString(),"*.xml").filesOnly()
				.getFiles().toArray(new File[0]);
			for (File file : files) {
				if (!file.canRead()) continue;
				String description = StringUtils.substringBeforeLast(file.getName(), ".xml");
				try (BufferedReader in = new BufferedReader(new FileReader(file))) {
					String line;
					while ((line = in.readLine()) != null) {
						if (StringUtils.contains(line, "<?xml")) break;
						String n = StringUtils.substringBefore(StringUtils.substringAfter(line, "SampleName=\""), "\"");
						if (n != null && n.length() > 0) {
							description = n;
							break;
						}
					}
					items.add(new SelectItem(file.getName(), description));
				} catch (Exception e) { throw e;} 
			}
			if (items.isEmpty()) throw new Exception("No sample messages to send");
			sampleMessages = items.toArray(new SelectItem[0]);
			Comparator<SelectItem> comp = new Util.CompSelectItem("label", true);
			Arrays.sort(sampleMessages, comp);
			log.trace(sampleMessages.length + " sample messages loaded");

		} catch (Exception e) {
			log.error(e.getMessage());
			throw new Exception(e.getMessage());
		}
	}

	// ------------------------------------------------
	// Getters and Setters
	// ------------------------------------------------

	public String getApplicationName() {
		return APPLICATION_NAME;
	}

	public String getDisplayName() {
		return Util.getDisplayName();
	}

	public SelectItem[] getXmitTypes() {
		return xmitTypes;
	}

	public SelectItem[] getSampleMessages() {
		return sampleMessages;
	}

	public void setSampleMessages(SelectItem[] sms) {
		sampleMessages = sms;
	}

	public Path getSampleMessageDirectory() {
		return sampleMessageDirectory;
	}

	public void setSampleMessageDirectory(Path sampleMessageDirectory) {
		this.sampleMessageDirectory = sampleMessageDirectory;
	}

	public SelectItem[] getCertificateNames() {
		return certificateNames;
	}

	public void setCertificateNames(SelectItem[] certificateNames) {
		this.certificateNames = certificateNames;
	}
	public Path getRunSyslogSender() {
		return runSyslogSender;
	}
	





} // EO ApplicationBean class
