package net.zsy.indexcreator.product;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.zsy.indexcreator.dispatch.AsyncIndexHandler;
import net.zsy.indexcreator.dispatch.Index;
import net.zsy.indexcreator.dispatch.IndexHandler;
import net.zsy.indexcreator.indexbean.Product;

/**
 * 商品类型索引创建处理器 异步、并发创建
 * 与sender共享一个阻塞队里，组成生产者-消费者
 */
public class ProductAsyncIndexHandler extends AsyncIndexHandler implements IndexHandler<ProductIndex> {

	//构造时将sender使用的队列传入
	public ProductAsyncIndexHandler(BlockingQueue<String> queue) {

		super(queue);

		Thread t = new Thread(new HandleThread());
		t.start();

	}

	// 异步消息队列
	private BlockingQueue<ProductIndex> asyncqueue = new LinkedBlockingQueue<ProductIndex>();

	@Override
	public void handle(ProductIndex t) {
		asyncqueue.add(t);
	}

	private class HandleThread implements Runnable {

		private ExecutorService exec = Executors.newFixedThreadPool(5);//写死5个线程，可做成配置或推导出一个公式进行计算

		private ObjectMapper objectMapper = new ObjectMapper();

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					final ProductIndex index = asyncqueue.take();
					exec.submit(new Runnable() {

						@Override
						public void run() {
							// 调用索引文档创建逻辑
							Product p = new Product();
							p.setId(index.getObjId());
							try {
								Map<String, Object> indexdata = new HashMap<String, Object>();
								indexdata.put(Index.ID, p.getId());
								indexdata.put(Index.TYPE, index.getType());
								indexdata.put(Index.TIMESTAMP, new Date().getTime());
								indexdata.put(Index.DATA, objectMapper.writeValueAsString(p));
								String jsondata = objectMapper.writeValueAsString(indexdata);
								//放入sender的队列
								queue.put(jsondata);
							} catch (JsonProcessingException e) {
								e.printStackTrace();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					});
				} catch (InterruptedException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}
			}
		}

	}
}
