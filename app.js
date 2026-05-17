// Design Ref: §3.1 — todos[] 단일 진실 공급원(SSOT), currentFilter 상태
let todos = [];
let currentFilter = 'all';

// ── Storage ──────────────────────────────────────────────────────────────────

function loadTodos() {
  try {
    todos = JSON.parse(localStorage.getItem('todos') || '[]');
  } catch {
    todos = [];
  }
}

function saveTodos() {
  localStorage.setItem('todos', JSON.stringify(todos));
}

// ── State Mutations ───────────────────────────────────────────────────────────

// Plan SC: FR-01 — 텍스트 입력 후 Enter/Add로 Todo 추가
function addTodo(text) {
  const trimmed = text.trim();
  if (!trimmed) return;
  todos.push({ id: Date.now(), text: trimmed, completed: false });
  saveTodos();
  render();
}

// Plan SC: FR-02 — 체크박스 클릭으로 완료/미완료 토글
function toggleTodo(id) {
  const todo = todos.find(t => t.id === id);
  if (todo) {
    todo.completed = !todo.completed;
    saveTodos();
    render();
  }
}

// Plan SC: FR-03 — × 버튼으로 개별 삭제
function deleteTodo(id) {
  todos = todos.filter(t => t.id !== id);
  saveTodos();
  render();
}

// Plan SC: FR-04 — 더블클릭 인라인 수정, 빈 값이면 삭제
function editTodo(id, newText) {
  const trimmed = newText.trim();
  if (!trimmed) {
    deleteTodo(id);
    return;
  }
  const todo = todos.find(t => t.id === id);
  if (todo) {
    todo.text = trimmed;
    saveTodos();
    render();
  }
}

// Plan SC: FR-06 — 완료 항목 일괄 삭제
function clearCompleted() {
  todos = todos.filter(t => !t.completed);
  saveTodos();
  render();
}

// ── Rendering ─────────────────────────────────────────────────────────────────

// Design Ref: §2.2 — render()가 currentFilter 기반으로 DOM 전체 갱신
function render() {
  const list = document.getElementById('todo-list');
  const itemsLeft = document.getElementById('items-left');
  const clearBtn = document.getElementById('clear-btn');
  const filterBtns = document.querySelectorAll('.filter-btn');

  // 필터 적용
  const filtered = todos.filter(t => {
    if (currentFilter === 'active') return !t.completed;
    if (currentFilter === 'completed') return t.completed;
    return true;
  });

  // 목록 렌더링 (Design Ref: §10.2 — innerHTML 미사용)
  list.replaceChildren();

  if (filtered.length === 0) {
    const empty = document.createElement('li');
    empty.className = 'empty-message';
    empty.textContent = currentFilter === 'completed'
      ? '완료된 항목이 없습니다'
      : currentFilter === 'active'
        ? '남은 할 일이 없습니다'
        : '할 일을 추가해 보세요';
    list.appendChild(empty);
  } else {
    filtered.forEach(todo => {
      list.appendChild(createTodoElement(todo));
    });
  }

  // Plan SC: FR-07 — 남은 항목 수 표시
  const activeCount = todos.filter(t => !t.completed).length;
  itemsLeft.textContent = `${activeCount} item${activeCount !== 1 ? 's' : ''} left`;

  // Clear 버튼 표시/숨김
  const hasCompleted = todos.some(t => t.completed);
  clearBtn.style.visibility = hasCompleted ? 'visible' : 'hidden';

  // 활성 필터 탭 강조
  filterBtns.forEach(btn => {
    btn.classList.toggle('active', btn.dataset.filter === currentFilter);
  });
}

function createTodoElement(todo) {
  const li = document.createElement('li');
  li.className = `todo-item${todo.completed ? ' todo-item--completed' : ''}`;
  li.dataset.id = todo.id;

  const checkbox = document.createElement('input');
  checkbox.type = 'checkbox';
  checkbox.checked = todo.completed;
  checkbox.addEventListener('change', () => toggleTodo(todo.id));

  // Design Ref: §10.2 — textContent 사용으로 XSS 방지
  const span = document.createElement('span');
  span.className = 'todo-text';
  span.textContent = todo.text;

  // Plan SC: FR-04 — 더블클릭으로 인라인 수정 활성화
  span.addEventListener('dblclick', () => activateEdit(li, todo));

  const deleteBtn = document.createElement('button');
  deleteBtn.className = 'delete-btn';
  deleteBtn.textContent = '×';
  deleteBtn.setAttribute('aria-label', '삭제');
  deleteBtn.addEventListener('click', () => deleteTodo(todo.id));

  li.appendChild(checkbox);
  li.appendChild(span);
  li.appendChild(deleteBtn);
  return li;
}

function activateEdit(li, todo) {
  const span = li.querySelector('.todo-text');
  const deleteBtn = li.querySelector('.delete-btn');

  const input = document.createElement('input');
  input.type = 'text';
  input.className = 'todo-edit-input';
  input.value = todo.text;

  let saved = false;

  function save() {
    if (saved) return;
    saved = true;
    editTodo(todo.id, input.value);
  }

  input.addEventListener('keydown', e => {
    if (e.key === 'Enter') save();
    if (e.key === 'Escape') { saved = true; render(); }
  });
  input.addEventListener('blur', save);

  span.replaceWith(input);
  deleteBtn.style.opacity = '0';
  input.focus();
  input.select();
}

// ── Initialization ────────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
  loadTodos();

  const todoInput = document.getElementById('todo-input');
  const addBtn = document.getElementById('add-btn');
  const clearBtn = document.getElementById('clear-btn');
  const filterBtns = document.querySelectorAll('.filter-btn');

  // Plan SC: FR-01 — Enter 키로 추가
  todoInput.addEventListener('keydown', e => {
    if (e.key === 'Enter') {
      addTodo(todoInput.value);
      todoInput.value = '';
    }
  });

  addBtn.addEventListener('click', () => {
    addTodo(todoInput.value);
    todoInput.value = '';
    todoInput.focus();
  });

  // Plan SC: FR-05 — All/Active/Completed 필터
  filterBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      currentFilter = btn.dataset.filter;
      render();
    });
  });

  // Plan SC: FR-06 — Clear completed
  clearBtn.addEventListener('click', clearCompleted);

  render();
});
