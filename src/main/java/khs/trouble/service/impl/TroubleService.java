package khs.trouble.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import khs.trouble.base.BaseService;
import khs.trouble.model.Target;
import khs.trouble.repository.TroubleRepository;
import khs.trouble.service.IServiceRegistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TroubleService extends BaseService<TroubleRepository, Target> {

	private Logger LOG = Logger.getLogger(TroubleService.class.getName());

	@Autowired
	IServiceRegistry registry;

	@Autowired
	EventService eventService;

	@Value("${trouble.token}")
	String token;

	@Value("${trouble.timeout:300000}")
	Long timeout;

	@Value("${blocking.threads:200}")
	Integer threads;
	
	
	public String randomKill(String ltoken) {
       
		String serviceName = randomService();
		eventService.randomKilled(serviceName);
		return kill(serviceName, ltoken);

	}

	public String kill(String serviceName, String ltoken) {

		if (token != ltoken) {
			throw new RuntimeException("Invalid Access Token");
		}

		String url = registry.lookup(serviceName) + "/trouble/kill";

		// invoke kill api...

		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.TEXT_PLAIN));
		headers.add("token", token);
		HttpEntity<String> entity = new HttpEntity<String>("parameters",
				headers);

		try {
			ResponseEntity<String> result = restTemplate.exchange(url,
					HttpMethod.GET, entity, String.class);

			eventService.killed(serviceName, url);

		} catch (Exception e) {

			eventService.attempted("Attempted to Kill service " + serviceName
					+ " at " + url + " Failed due to exception "
					+ e.getMessage());
		}

		// ResponseEntity<T> result = restTemplate.getForEntity(url, type);
		// return result.getBody();

		return serviceName;

	}

	public String randomLoad(String ltoken) {

		return load(randomService(), ltoken);

	}

	public String load(String serviceName, String ltoken) {

		if (token != ltoken) {
			throw new RuntimeException("Invalid Access Token");
		}

		for (int i = 0; i < threads; i++) {
			LOG.info("Starting Thread " + i);
			spawnLoadThread(serviceName, 1000);
		}

		String url = registry.lookup(serviceName) + "trouble/load";

		eventService.load(serviceName, url,threads);

		// String url = registry.lookup(serviceName) + "/trouble/block";

		// invoke kill api...
		//
		// RestTemplate restTemplate = new RestTemplate();
		//
		// HttpHeaders headers = new HttpHeaders();
		// headers.setAccept(Arrays.asList(MediaType.TEXT_PLAIN));
		// headers.add("token", token);
		// headers.add("timeout", "" + timeout);
		// HttpEntity<String> entity = new HttpEntity<String>("parameters",
		// headers);
		//
		// try {
		//
		// ResponseEntity<String> result = restTemplate.exchange(url,
		// HttpMethod.GET, entity, String.class);
		//
		// eventService.blocked(serviceName,url);
		//
		// } catch (Exception e) {
		//
		// eventService.attempted("Attempted to Block service " + serviceName
		// + " at " + url + " Failed due to exception "
		// + e.getMessage());
		// }

		return serviceName;

	}

	public String randomService() {

		Random rn = new Random();

		List<String> list = registry.serviceNames();

		int range = list.size();
		int randomNum = rn.nextInt(range);

		return list.get(randomNum);

	}
	
	public String randomException(String ltoken) {

		return exception(randomService(), ltoken);

	}
		
	public String exception(String serviceName,String ltoken) {
		
		if (token != ltoken) {
			throw new RuntimeException("Invalid Access Token");
		}

		String url = registry.lookup(serviceName) + "/trouble/exception";

		// invoke kill api...

		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.TEXT_PLAIN));
		headers.add("token", token);
		HttpEntity<String> entity = new HttpEntity<String>("parameters",
				headers);

		try {
			ResponseEntity<String> result = restTemplate.exchange(url,
					HttpMethod.GET, entity, String.class);

			eventService.exception(serviceName, url);

		} catch (Exception e) {

			eventService.attempted("Attempted to throw exception at service " + serviceName
					+ " at " + url + " Failed due to exception "
					+ e.getMessage());
		}


		return serviceName;
	
	}
	
    
	public String randomMemory(String ltoken) {

		return memory(randomService(), ltoken);

	}
	
	
	public String memory(String serviceName,String ltoken) {
		
		if (token != ltoken) {
			throw new RuntimeException("Invalid Access Token");
		}

		String url = registry.lookup(serviceName) + "/trouble/memory";

		// invoke memory api...

		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.TEXT_PLAIN));
		headers.add("token", token);
		headers.add("timeout", "" + timeout);
		HttpEntity<String> entity = new HttpEntity<String>("parameters",
				headers);

		try {
			ResponseEntity<String> result = restTemplate.exchange(url,
					HttpMethod.GET, entity, String.class);

			eventService.memory(serviceName, url);

		} catch (Exception e) {

			eventService.attempted("Attempted to consume memory at service " + serviceName
					+ " at " + url + " Failed due to exception "
					+ e.getMessage());
		}


		return serviceName;
	
	}
	
	
	
	
	
	

	public void spawnLoadThread(String serviceName, final long sleep) {

		Runnable run = new Runnable() {

			public void run() {
				try {

					String url = registry.lookup(serviceName)
							+ "/trouble/load";

					// invoke kill api...

					RestTemplate restTemplate = new RestTemplate();

					HttpHeaders headers = new HttpHeaders();
					headers.setAccept(Arrays.asList(MediaType.TEXT_PLAIN));
					headers.add("token", token);
					headers.add("timeout", "" + timeout);
					HttpEntity<String> entity = new HttpEntity<String>(
							"parameters", headers);

					try {

						ResponseEntity<String> result = restTemplate.exchange(
								url, HttpMethod.GET, entity, String.class);

					} catch (Exception e) {

						eventService.attempted("Attempted to Block service "
								+ serviceName + " at " + url
								+ " Failed due to exception " + e.getMessage());
					}

					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		};

		Thread thread = new Thread(run);
		thread.start();
	}

}