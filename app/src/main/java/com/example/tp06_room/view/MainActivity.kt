package com.example.tp06_room.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.tp06_room.dao.ProductDao
import com.example.tp06_room.databinding.ActivityMainBinding
import com.example.tp06_room.db.AppDatabase
import com.example.tp06_room.model.Product
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var productDao: ProductDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        )
            .fallbackToDestructiveMigration(true) // recria o DB se o esquema mudar (apaga os dados)
            .build()
        productDao = db.productDao()

        binding.btnInserirProduto.setOnClickListener { inserirProduto() }
        binding.btnListarProdutos.setOnClickListener {
            lifecycleScope.launch { mostrarLista(productDao.getAll()) }
        }
        binding.btnBuscar.setOnClickListener {
            val texto = binding.editBusca.text.toString().trim()
            lifecycleScope.launch { mostrarLista(productDao.buscarPorNome("%$texto%")) }
        }
        binding.btnExcluirTodos.setOnClickListener {
            lifecycleScope.launch {
                productDao.deleteAll()
                mostrarLista(emptyList())
                toast("Todos os produtos foram excluídos!")
            }
        }
        binding.btnAtualizarPreco.setOnClickListener { atualizarPreco() }
        binding.btnExcluirPorId.setOnClickListener { excluirPorId() }
    }

    private fun inserirProduto() {
        val name = binding.editProductName.text.toString().trim()
        val price = parsePreco(binding.editProductPrice.text.toString())
        val description = binding.editProductDescription.text.toString()
        val quantity = binding.editProductQuantity.text.toString().toIntOrNull() ?: 0

        if (name.isEmpty()) {
            binding.editProductName.error = "Informe o nome"
            return
        }
        // Validação: preço vazio, zero ou negativo não é permitido
        if (price == null || price <= 0) {
            binding.editProductPrice.error = "Informe um preço válido"
            return
        }

        val product = Product(name = name, price = price, description = description, quantity = quantity)
        lifecycleScope.launch {
            productDao.insertAll(product)
            Log.i("TAG", "Produto inserido: $name - R$$price")
            toast("Produto inserido!")

            // Limpa os campos e devolve o cursor ao campo Nome
            binding.editProductName.text.clear()
            binding.editProductPrice.text.clear()
            binding.editProductDescription.text.clear()
            binding.editProductQuantity.text.clear()
            binding.editProductName.requestFocus()
        }
    }

    private fun atualizarPreco() {
        val id = binding.editUpdateId.text.toString().toIntOrNull()
        val novoPreco = parsePreco(binding.editUpdatePrice.text.toString())
        if (id == null) {
            binding.editUpdateId.error = "Informe o id"
            return
        }
        if (novoPreco == null || novoPreco <= 0) {
            binding.editUpdatePrice.error = "Informe um preço válido"
            return
        }
        lifecycleScope.launch {
            val produto = productDao.getById(id)
            if (produto == null) {
                toast("Produto $id não encontrado")
                return@launch
            }
            productDao.update(produto.copy(price = novoPreco))
            toast("Preço atualizado!")
            mostrarLista(productDao.getAll())
        }
    }

    private fun excluirPorId() {
        val id = binding.editUpdateId.text.toString().toIntOrNull()
        if (id == null) {
            binding.editUpdateId.error = "Informe o id"
            return
        }
        lifecycleScope.launch {
            val produto = productDao.getById(id)
            if (produto == null) {
                toast("Produto $id não encontrado")
                return@launch
            }
            productDao.delete(produto)
            toast("Produto excluído!")
            mostrarLista(productDao.getAll())
        }
    }

    private fun mostrarLista(products: List<Product>) {
        products.forEach {
            Log.i("TAG", "Produto = ${it.name}, Preço = R$${it.price}, Descrição = ${it.description}, Qtd = ${it.quantity}")
        }
        if (products.isEmpty()) {
            binding.txtResultado.text = "Nenhum produto encontrado.\n\nTotal: 0 produto(s) | Soma dos preços: R$ 0,00"
            return
        }
        val linhas = products.joinToString("\n") {
            "#${it.id} ${it.name} - R$ ${formata(it.price)} - Qtd: ${it.quantity}" +
                if (it.description.isNullOrBlank()) "" else "\n    ${it.description}"
        }
        binding.txtResultado.text =
            "$linhas\n\nTotal: ${products.size} produto(s) | Soma dos preços: R$ ${formata(products.sumOf { it.price })}"
    }

    private fun parsePreco(texto: String): Double? = texto.trim().replace(',', '.').toDoubleOrNull()

    private fun formata(valor: Double) = String.format(Locale("pt", "BR"), "%.2f", valor)

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
