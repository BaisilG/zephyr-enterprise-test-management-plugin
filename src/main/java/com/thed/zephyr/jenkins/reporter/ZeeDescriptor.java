package com.thed.zephyr.jenkins.reporter;

import static com.thed.zephyr.jenkins.reporter.ZeeConstants.ADD_ZEPHYR_GLOBAL_CONFIG;
import static com.thed.zephyr.jenkins.reporter.ZeeConstants.CYCLE_DURATION_1_DAY;
import static com.thed.zephyr.jenkins.reporter.ZeeConstants.CYCLE_DURATION_30_DAYS;
import static com.thed.zephyr.jenkins.reporter.ZeeConstants.CYCLE_DURATION_7_DAYS;
import static com.thed.zephyr.jenkins.reporter.ZeeConstants.NAME_POST_BUILD_ACTION;
import static com.thed.zephyr.jenkins.reporter.ZeeConstants.NEW_CYCLE_KEY;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.thed.service.*;
import com.thed.service.impl.*;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConfigurationException;

import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.thed.zephyr.jenkins.model.ZephyrConfigModel;
import com.thed.zephyr.jenkins.model.ZephyrInstance;
import com.thed.zephyr.jenkins.utils.ConfigurationValidator;
import com.thed.zephyr.jenkins.utils.URLValidator;
import com.thed.zephyr.jenkins.utils.ZephyrSoapClient;
import com.thed.zephyr.jenkins.utils.rest.Cycle;
import com.thed.zephyr.jenkins.utils.rest.Project;
import com.thed.zephyr.jenkins.utils.rest.Release;
import com.thed.zephyr.jenkins.utils.rest.RestClient;
import com.thed.zephyr.jenkins.utils.rest.ServerInfo;
import org.kohsuke.stapler.verb.POST;


@Extension
public final class ZeeDescriptor extends BuildStepDescriptor<Publisher> {

	private static Logger logger = Logger.getLogger(ZeeDescriptor.class.getName());

    ZephyrRestService zephyrRestService;

    private UserService userService = new UserServiceImpl();
    private ProjectService projectService = new ProjectServiceImpl();
    private ReleaseService releaseService = new ReleaseServiceImpl();
    private CycleService cycleService = new CycleServiceImpl();

	private List<ZephyrInstance> zephyrInstances;

	public List<ZephyrInstance> getZephyrInstances() {
		return zephyrInstances;
	}

	public void setZephyrInstances(List<ZephyrInstance> zephyrInstances) {
		this.zephyrInstances = zephyrInstances;
	}

	public ZeeDescriptor() {
		super(ZeeReporter.class);
		load();

        zephyrRestService = new ZephyrRestServiceImpl();
	}

	@Override
	public Publisher newInstance(StaplerRequest req, JSONObject formData)
			throws FormException {
		return super.newInstance(req, formData);
	}

	@Override
	public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
		return true;
	}

	@Override
	public boolean configure(StaplerRequest req, JSONObject formData)
			throws FormException {
		req.bindParameters(this);
		
		logger.info("Displaying Zephyr server config section");
		
		this.zephyrInstances = new ArrayList<ZephyrInstance>();
		Object object = formData.get("zephyrInstances");
		if (object instanceof JSONArray) {
			JSONArray jArr = (JSONArray) object;
			for (Iterator iterator = jArr.iterator(); iterator.hasNext();) {
				JSONObject jObj = (JSONObject) iterator.next();
				ZephyrInstance zephyrInstance = new ZephyrInstance();

				RestClient restClient = null;
				try {
				String server = URLValidator.validateURL(jObj.getString("serverAddress").trim());
				String user = jObj.getString("username").trim();
				String pass = jObj.getString("password").trim();

				zephyrInstance.setServerAddress(server);
				zephyrInstance.setUsername(user);
				zephyrInstance.setPassword(pass);
				boolean zephyrServerValidation = false;
					restClient = new RestClient(server, user, pass);
					zephyrServerValidation = ConfigurationValidator.validateZephyrConfiguration(restClient, getZephyrRestVersion(restClient));
					if (zephyrServerValidation) {
						this.zephyrInstances.add(zephyrInstance);
					}
				} catch (Throwable e) {
					logger.log(Level.ALL, "Error in validating server and credentials. ");
					logger.log(Level.ALL, e.getMessage());
				}	finally {
					closeHTTPClient(restClient);
				}
			}

		} else if (object instanceof JSONObject) {
			JSONObject jObj = formData.getJSONObject("zephyrInstances");
			ZephyrInstance zephyrInstance = new ZephyrInstance();

			RestClient restClient = null;
			try {
			String server = URLValidator.validateURL(jObj.getString("serverAddress").trim());
			String user = jObj.getString("username").trim();
			String pass = jObj.getString("password").trim();

			zephyrInstance.setServerAddress(server);
			zephyrInstance.setUsername(user);
			zephyrInstance.setPassword(pass);

			boolean zephyrServerValidation = false;
				restClient = new RestClient(server, user, pass);
				zephyrServerValidation = ConfigurationValidator
                        .validateZephyrConfiguration(restClient, getZephyrRestVersion(restClient));
				if (zephyrServerValidation) {
					this.zephyrInstances.add(zephyrInstance);
				}
			} catch (Throwable e) {
				logger.log(Level.ALL, "Error in validating server and credentials. ");
				logger.log(Level.ALL, e.getMessage());
			} finally {
				closeHTTPClient(restClient);
			}

		}
		save();
		return super.configure(req, formData);
	}

	/**
	 *
	 */
	private void closeHTTPClient(RestClient restClient) {
		if(restClient != null) {
			restClient.destroy();
		}
	}

	@Override
	public String getDisplayName() {
		return NAME_POST_BUILD_ACTION;
	}

//	public FormValidation doCheckProjectKey(@QueryParameter String value) {
//		if (value.isEmpty()) {
//			return FormValidation.error("You must provide a project key.");
//		} else {
//			return FormValidation.ok();
//		}
//	}

    @POST
	public FormValidation doTestConnection(
			@QueryParameter String serverAddress,
			@QueryParameter String username, @QueryParameter String password) {

        Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);

//        try {
//            zephyrRestService.login("http://localhost:8080", "test.manager", "test.manager");
////            com.thed.model.Project project = zephyrRestService.getProjectById(1l);
////            System.out.println(project);
//            com.thed.model.Cycle cycle = new com.thed.model.Cycle();
//            cycle.setName("tester cycle");
//            cycle.setReleaseId(1l);
//            cycle.setStartDate(new Date());
//            cycle.setEndDate(new Date());
//
//            com.thed.model.Cycle cycle1 = zephyrRestService.createCycle(cycle);
//
//            GsonBuilder gsonBuilder = new GsonBuilder();
//            Gson gson = gsonBuilder.create();
//            String json = gson.toJson(cycle1);
//            System.out.println(json);
//        } catch(Exception e) {
//            e.printStackTrace();
//        }



		if (StringUtils.isBlank(serverAddress)) {
			return FormValidation.error("Please enter the server name");
		}

		if (StringUtils.isBlank(username)) {
			return FormValidation.error("Please enter the username");
		}

		if (StringUtils.isBlank(password)) {
			return FormValidation.error("Please enter the password");
		}

		if (!(serverAddress.trim().startsWith("https://") || serverAddress
				.trim().startsWith("http://"))) {
			return FormValidation.error("Incorrect server address format");
		}

		String zephyrURL = URLValidator.validateURL(serverAddress);
//		Map<Boolean, String> credentialValidationResultMap;
//		RestClient restClient = null;
//		try {
//	    	restClient = new RestClient(serverAddress, username, password);
//
//			if (!zephyrURL.startsWith("http")) {
//                return FormValidation.error(zephyrURL);
//            }
//
//			if (!ServerInfo.findServerAddressIsValidZephyrURL(restClient)) {
//                return FormValidation.error("This is not a valid Zephyr Server");
//            }
//
//			credentialValidationResultMap = ServerInfo
//                    .validateCredentials(restClient, getZephyrRestVersion(restClient));
//		} finally {
//			closeHTTPClient(restClient);
//		}
//		if (credentialValidationResultMap.containsKey(false)) {
//			return FormValidation.error(credentialValidationResultMap
//					.get(false));
//		}

        try {
            Boolean verifyCredentials = userService.verifyCredentials(serverAddress, username, password);
            if(!verifyCredentials) {
                return FormValidation.error("Username or password is incorrect.");
            }
        } catch (Exception e) {
            return FormValidation.error("Error occurred while verifying credentials. Please try again later.");
        }

		return FormValidation.ok("Connection to Zephyr has been validated");
	}

    private String getZephyrRestVersion(RestClient restClient) {
//        String zephyrVersion = ServerInfo.findZephyrVersion(restClient);
        String zephyrRestVersion = "v1";
//			if (zephyrVersion.equals("4.8") || zephyrVersion.equals("5.0")) {
//				zephyrRestVersion = "v1";
//			} else {
//				zephyrRestVersion = "latest";
//			}
//
        return zephyrRestVersion;
    }

	public ListBoxModel doFillServerAddressItems(
			@QueryParameter String serverAddress) {
		return fetchServerList(serverAddress);
	}

	private ListBoxModel fetchServerList(String serverAddress) {
		ListBoxModel m = new ListBoxModel();

		if (this.zephyrInstances.size() > 0) {

			for (ZephyrInstance s : this.zephyrInstances) {
				m.add(s.getServerAddress());
			}
		} else if (StringUtils.isBlank(serverAddress)
				|| serverAddress.trim().equals(ADD_ZEPHYR_GLOBAL_CONFIG)) {
			m.add(ADD_ZEPHYR_GLOBAL_CONFIG);
		} else {
			m.add(ADD_ZEPHYR_GLOBAL_CONFIG);
		}
		return m;
	}

	public ListBoxModel doFillProjectKeyItems(
			@QueryParameter String serverAddress) {
		return fetchProjectList(serverAddress);
	}

	private ListBoxModel fetchProjectList(String serverAddress) {
		ListBoxModel m = new ListBoxModel();

		if (StringUtils.isBlank(serverAddress)) {
	        ListBoxModel mi = fetchServerList(serverAddress);
			serverAddress = mi.get(0).value;
		}
		if (serverAddress.trim().equals(ADD_ZEPHYR_GLOBAL_CONFIG)
				|| (this.zephyrInstances.size() == 0)) {
			m.add(ADD_ZEPHYR_GLOBAL_CONFIG);
			return m;
		}

//      Legacy
//		RestClient restClient = null;
//		Map<Long, String> projects;
//		try {
//	    	restClient = getRestclient(serverAddress);
//			projects = Project.getAllProjects(restClient, getZephyrRestVersion(restClient));
//		} finally {
//			closeHTTPClient(restClient);
//		}
//		Set<Entry<Long, String>> projectEntrySet = projects.entrySet();
//
//		for (Iterator<Entry<Long, String>> iterator = projectEntrySet
//				.iterator(); iterator.hasNext();) {
//			Entry<Long, String> entry = iterator.next();
//			m.add(entry.getValue(), entry.getKey() + "");
//		}

        try {

            ZephyrInstance zephyrInstance = fetchZephyrInstance(serverAddress);
            userService.login(zephyrInstance.getServerAddress(), zephyrInstance.getUsername(), zephyrInstance.getPassword());

            List<com.thed.model.Project> projects = projectService.getAllProjectsForCurrentUser();
            for (com.thed.model.Project project : projects) {
                if(project.getGlobalProject() == Boolean.FALSE) {
                    m.add(project.getName(), project.getId().toString());
                }
            }
        }
        catch(Exception e) {
            //Todo: handle exceptions gracefully
            e.printStackTrace();
        }

		return m;
	}

	/**
	 * @param serverAddress
	 */
	private ZephyrInstance fetchZephyrInstance(String serverAddress) {
		
		ZephyrInstance zephyrInstance = new ZephyrInstance();
		zephyrInstance.setServerAddress(serverAddress);
		String tempUserName = null;
		String tempPassword = null;

		for (ZephyrInstance z : zephyrInstances) {
			if (z.getServerAddress().trim().equals(serverAddress)) {
				tempUserName = z.getUsername();
				tempPassword = z.getPassword();
			}
		}
		zephyrInstance.setUsername(tempUserName);
		zephyrInstance.setPassword(tempPassword);
		return zephyrInstance;
		
		
	}

	public ListBoxModel doFillReleaseKeyItems(
			@QueryParameter String projectKey,
			@QueryParameter String serverAddress) {

		return fetchReleaseList(projectKey, serverAddress);

	}

	private ListBoxModel fetchReleaseList(String projectKey, String serverAddress) {
		ListBoxModel listBoxModel = new ListBoxModel();

		if (StringUtils.isBlank(serverAddress)) {
	        ListBoxModel mi = fetchServerList(serverAddress);
			serverAddress = mi.get(0).value;
		}
		if (StringUtils.isBlank(projectKey)) {
	        ListBoxModel mi = fetchProjectList(serverAddress);
	        projectKey = mi.get(0).value;
		}

		if (projectKey.trim().equals(ADD_ZEPHYR_GLOBAL_CONFIG)
				|| (this.zephyrInstances.size() == 0)) {
			listBoxModel.add(ADD_ZEPHYR_GLOBAL_CONFIG);
			return listBoxModel;
		}

//		long parseLong = 0;
//		try {
//			parseLong = Long.parseLong(projectKey);
//		} catch (NumberFormatException e) {
//			return listBoxModel;
//		}
//		RestClient restClient = null;
//		Map<Long, String> releases;
//		try {
//	    	restClient = getRestclient(serverAddress);
//			releases = Release.getAllReleasesByProjectID(parseLong,restClient, getZephyrRestVersion(restClient));
//		} finally {
//			closeHTTPClient(restClient);
//		}
//		Set<Entry<Long, String>> releaseEntrySet = releases.entrySet();
//
//		for (Iterator<Entry<Lo             context) {ng, String>> iterator = releaseEntrySet
//				.iterator(); iterator.hasNext();) {
//			Entry<Long, String> entry = iterator.next();
//			listBoxModel.add(entry.getValue(), entry.getKey() + "");
//		}

        try {
            Long projectId = Long.parseLong(projectKey);
            List<com.thed.model.Release> releases = releaseService.getAllReleasesForProjectId(projectId);
            for (com.thed.model.Release release : releases) {
                listBoxModel.add(release.getName(), release.getId().toString());
            }
        }
        catch(Exception e) {
            //todo: handle exception gracefully
            e.printStackTrace();
        }

		return listBoxModel;
	}

	public ListBoxModel doFillCycleKeyItems(@QueryParameter String releaseKey, @QueryParameter String projectKey,
											@QueryParameter String serverAddress) {

		ListBoxModel listBoxModel = new ListBoxModel();
		
		if (StringUtils.isBlank(serverAddress)) {
	        ListBoxModel mi = fetchServerList(serverAddress);
			serverAddress = mi.get(0).value;
		}

		if (StringUtils.isBlank(releaseKey)) {
	        ListBoxModel mi = fetchReleaseList(projectKey, serverAddress);
	        releaseKey = mi.get(0).value;
		}


		if (releaseKey.trim().equals(ADD_ZEPHYR_GLOBAL_CONFIG)
				|| (this.zephyrInstances.size() == 0)) {
			listBoxModel.add(ADD_ZEPHYR_GLOBAL_CONFIG);
			return listBoxModel;
		}

//		long parseLong;
//		try {
//			parseLong = Long.parseLong(releaseKey);
//		} catch (NumberFormatException e) {
//			return listBoxModel;
//		}
//
//		RestClient restClient = null;
//		Map<Long, String> cycles;
//		try {
//	    	restClient = getRestclient(serverAddress);
//			cycles = Cycle.getAllCyclesByReleaseID(parseLong, restClient, getZephyrRestVersion(restClient));
//		} finally {
//			closeHTTPClient(restClient);
//		}
//
//		Set<Entry<Long, String>> releaseEntrySet = cycles.entrySet();
//
//		for (Iterator<Entry<Long, String>> iterator = releaseEntrySet
//				.iterator(); iterator.hasNext();) {
//			Entry<Long, String> entry = iterator.next();
//			listBoxModel.add(entry.getValue(), entry.getKey() + "");
//		}

        try {
            Long releaseId = Long.parseLong(releaseKey);
            List<com.thed.model.Cycle> cycles = cycleService.getAllCyclesForReleaseId(releaseId);
            for (com.thed.model.Cycle cycle : cycles) {
                listBoxModel.add(cycle.getName(), cycle.getId().toString());
            }
        }
        catch(Exception e) {
            //todo: handle exceptions gracefully
            e.printStackTrace();
        }

		listBoxModel.add("New Cycle", NEW_CYCLE_KEY);

		return listBoxModel;
	}

	public ListBoxModel doFillCycleDurationItems(
			@QueryParameter String serverAddress,
			@QueryParameter String projectKey) {

		ListBoxModel listBoxModel = new ListBoxModel();
//		long zephyrProjectId;
//		try {
//			zephyrProjectId = Long.parseLong(projectKey);
//		} catch (NumberFormatException e1) {
//			listBoxModel.add(CYCLE_DURATION_1_DAY);
//			return listBoxModel;
//		}
//		ZephyrConfigModel zephyrData = new ZephyrConfigModel();
//		zephyrData.setZephyrProjectId(zephyrProjectId);
//		int fetchProjectDuration = 1;
//
//		zephyrData.setSelectedZephyrServer(fetchZephyrInstance(serverAddress));
//		try {
//			fetchProjectDuration = ZephyrSoapClient
//					.fetchProjectDuration(zephyrData);
//
//		} catch (DatatypeConfigurationException e) {
//			e.printStackTrace();
//		}

        try {
            Long projectId = Long.parseLong(projectKey);
            Long projectDuration = projectService.getProjectDurationInDays(projectId);

            if (projectDuration == -1) {
                listBoxModel.add(CYCLE_DURATION_30_DAYS);
                listBoxModel.add(CYCLE_DURATION_7_DAYS);
                listBoxModel.add(CYCLE_DURATION_1_DAY);
                return listBoxModel;
            }

            if (projectDuration >= 29) {
                listBoxModel.add(CYCLE_DURATION_30_DAYS);
            }

            if (projectDuration >= 6) {
                listBoxModel.add(CYCLE_DURATION_7_DAYS);
            }
        }
        catch (Exception e) {
            //todo: handle exception gracefully
            e.printStackTrace();
        }

		listBoxModel.add(CYCLE_DURATION_1_DAY);
		return listBoxModel;
	}
	
	private RestClient getRestclient(String serverAddress) {
		String tempUserName = null;
		String tempPassword = null;
		for (ZephyrInstance z: zephyrInstances) {
    		if(z.getServerAddress().trim().equals(serverAddress)) {
    			tempUserName = z.getUsername();
    			tempPassword = z.getPassword();
    			break;
    		}
    	}
			RestClient restClient = new RestClient(serverAddress, tempUserName, tempPassword);
			
			return restClient;
	}
}