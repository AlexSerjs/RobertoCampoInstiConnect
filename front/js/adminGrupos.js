function openGrupoModal() {
    const modal = document.getElementById('modal-form');
    const modalBody = document.getElementById('modal-body');
  
    modalBody.innerHTML = `
      <h2>Formulario para Grupos</h2>
      <form>
        <label for="nombre-grupo">Nombre del Grupo:</label><br>
        <input type="text" id="nombre-grupo" name="nombre-grupo"><br><br>
        <label for="descripcion">Descripción:</label><br>
        <textarea id="descripcion" name="descripcion"></textarea><br><br>
        <button type="submit">Enviar</button>
      </form>
    `;
    modal.classList.remove('hidden');
  }
  function closeModal() {
    const modal = document.getElementById('modal-form');
    modal.classList.add('hidden');
  }
  
  