
describe('Product CRUD', () => {
	const mongoose = require('mongoose');
	let server;
	let connection;
	let db;

	beforeAll(async () => {
		connection = await mongoose.connect('mongodb://localhost:27017/test_', { useNewUrlParser: true, useUnifiedTopology: true });
		db = mongoose.connection;
		const collection = 'test_product';
		await db.createCollection(collection);
		server = await require('../src/index');
		await server.ready();
	});

	afterAll(async () => {
		const collection = 'test_product';
		await db.dropCollection(collection);
		await db.dropDatabase();
		await db.close();
		await connection.close();
		server.close();
	});

	test('Add Product POST /product', async (done) => {
		const response = await server.inject({
			method: 'POST',
			url: '/product',
			payload: {
				_id: '5f2678dff22e1f4a3c0782ee',
				name: 'JBL Headphone',
				category: 'Electronic appliances',
				unit: 1
			}
		});
		expect(response.statusCode).toBe(201);
		done();
	});

	test('Get All Product /product', async (done) => {
		const response = await server.inject({
			method: 'GET',
			url: '/product'
		});
		expect(response.statusCode).toBe(200);
		done();
	});

	test('Update Product PUT /product/:id', async (done) => {
		const response = await server.inject({
			method: 'PUT',
			url: '/product/5f2678dff22e1f4a3c0782ee',
			payload: {
				unit: 2
			}
		});
		expect(response.statusCode).toBe(200);
		done();
	});

	test('Get one Product GET /product/:id', async (done) => {
		const response = await server.inject({
			method: 'GET',
			url: '/product/5f2678dff22e1f4a3c0782ee'
		});
		expect(response.statusCode).toBe(200);
		done();
	});

	test('Delete one Product DELETE /product/:_id', async (done) => {
		const response = await server.inject({
			method: 'DELETE',
			url: '/product/5f2678dff22e1f4a3c0782ee'
		});
		expect(response.statusCode).toBe(200);
		done();
	});

	test('Health Route', async (done) => {
		const response = await server.inject({
			method: 'GET',
			url: '/health'
		});
		expect(response.statusCode).toBe(200);
		done();
	});

});