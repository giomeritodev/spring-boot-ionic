package com.giomerito.cursomc.services;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.giomerito.cursomc.domain.ItemPedido;
import com.giomerito.cursomc.domain.PagamentoComBoleto;
import com.giomerito.cursomc.domain.Pedido;
import com.giomerito.cursomc.domain.enums.EstadoPagamento;
import com.giomerito.cursomc.repositories.ItemPedidoRepository;
import com.giomerito.cursomc.repositories.PagamentoRepository;
import com.giomerito.cursomc.repositories.PedidoRepository;
import com.giomerito.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class PedidoService {
	
	@Autowired //esta dependência vai ser automaticamente instânciada pelo spring
	private PedidoRepository repo;
	@Autowired
	private BoletoService boletoService;
	@Autowired
	private PagamentoRepository pagamentoRepository;
	@Autowired
	private ProdutoService produtoService;
	@Autowired 
	private ItemPedidoRepository itemPedidoRepository;

	
	public Pedido find(Integer id) {
		Pedido obj = repo.findOne(id);
		if(obj == null) { //Lança uma Exception
			throw new ObjectNotFoundException("Objeto não encontrado! Id: " + id 
					+ ", Tipo: " + Pedido.class.getName());
		}
		return obj;
	}
	
	@Transactional
	public Pedido insert(Pedido obj) {
		obj.setId(null);
		obj.setInstante(new Date());
		obj.getPagamento().setEstado(EstadoPagamento.PENDENTE);;
		obj.getPagamento().setPedido(obj);
		if (obj.getPagamento() instanceof PagamentoComBoleto) {
			PagamentoComBoleto pagto = (PagamentoComBoleto) obj.getPagamento();
			boletoService.preencherPagamentoComBoleto(pagto, obj.getInstante());
		}
		obj = repo.save(obj);
		pagamentoRepository.save(obj.getPagamento());
		for(ItemPedido ip : obj.getItens()) {
			ip.setDesconto(0.0);
			ip.setPreco(produtoService.find(ip.getProduto().getId()).getPreco());
			ip.setPedido(obj);
		}
		itemPedidoRepository.save(obj.getItens());
		return obj;
	}
	
}
