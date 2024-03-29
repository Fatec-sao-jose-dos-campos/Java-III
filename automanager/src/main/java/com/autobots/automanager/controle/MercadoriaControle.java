package com.autobots.automanager.controle;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entitades.Empresa;
import com.autobots.automanager.entitades.Mercadoria;
import com.autobots.automanager.entitades.Usuario;
import com.autobots.automanager.modelos.SelecionadorMercadoria;
import com.autobots.automanager.modelos.SelecionadorUsuario;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioMercadoria;
import com.autobots.automanager.repositorios.RepositorioUsuario;

@RestController
@RequestMapping("/mercadoria")
public class MercadoriaControle {
	@Autowired
	private RepositorioEmpresa repoEmpresa;
	@Autowired
	private SelecionadorUsuario selecionadorUsu;
	@Autowired
	private RepositorioUsuario repoUsuario;
	@Autowired
	private RepositorioMercadoria repoMercadoria;
	@Autowired
	private SelecionadorMercadoria selecionador;
	@GetMapping ("/todos")
	public ResponseEntity<?> pegarTodasMercadorias (){
		List<Mercadoria> todos = repoMercadoria.findAll();
		HttpStatus status = HttpStatus.ACCEPTED;
		if (todos.isEmpty()) {
			status = HttpStatus.NOT_FOUND;
			return new ResponseEntity<>(status);
		}else {
			return new ResponseEntity<>(todos,status);
		}
		
	}
	@GetMapping ("/todos/{mercadoria}")
	public ResponseEntity<?> pegarMercadoria (@PathVariable Long mercadoria){
		Mercadoria selecionado = selecionador.select(repoMercadoria.findAll(), mercadoria);
		HttpStatus status = HttpStatus.I_AM_A_TEAPOT;
		if (selecionado == null) {
			status = HttpStatus.NOT_FOUND;
			return new ResponseEntity<>(status);
		}else {
			status = HttpStatus.FOUND;
			return new ResponseEntity<>(selecionado, status);
		}
	}
	
	@PutMapping ("/atualizar/{id}")
	public ResponseEntity<?> atualizaMercadoria (@PathVariable Long id, @RequestBody Mercadoria atualizador){
		Mercadoria selecionado = selecionador.select(repoMercadoria.findAll(), id);
		HttpStatus status = HttpStatus.I_AM_A_TEAPOT;
		if (selecionado == null) {
			status = HttpStatus.NOT_FOUND;
			return new ResponseEntity<>(status);
		}else {
			selecionado.setNome(atualizador.getNome());
			selecionado.setValidade(atualizador.getValidade());
			selecionado.setFabricao(atualizador.getFabricao());
			selecionado.setCadastro(atualizador.getCadastro());
			selecionado.setQuantidade(atualizador.getQuantidade());
			selecionado.setValor(atualizador.getValor());
			selecionado.setDescricao(atualizador.getDescricao());
			repoMercadoria.save(selecionado);
			status = HttpStatus.FOUND;
			return new ResponseEntity<>(selecionado, status);
		}
			
}
	
	@PostMapping("/cadastro/{idUsuario}")
	public ResponseEntity<?> cadastroMercadoria(
		@RequestBody Mercadoria cadastro,
		@PathVariable Long idUsuario
	){
		
		Long idTeste = repoMercadoria.save(cadastro).getId();
		Mercadoria mercadoria = repoMercadoria.findById(idTeste).orElse(null);
		
		List<Usuario> usuarios = repoUsuario.findAll();
		Usuario select = selecionadorUsu.select(usuarios, idUsuario);
		if (select != null) {
			for(Empresa empresas : repoEmpresa.findAll()) {
				for(Usuario usuario : empresas.getUsuarios()) {
					if(usuario.getId().equals(idUsuario)) {
						empresas.getMercadorias().add(mercadoria);
						repoEmpresa.save(empresas);
						break;
					}
				}
			}
			select.getMercadorias().add(mercadoria);
			repoUsuario.save(select);
			return new ResponseEntity<>(
				"Mercadoria Cadastrada com sucesso",
				HttpStatus.CREATED
			);
		}else {
			return new ResponseEntity<>("Usuário não encontrado", HttpStatus.NOT_FOUND);
		}
	}
}
