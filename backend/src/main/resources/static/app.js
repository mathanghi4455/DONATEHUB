// ===================================================================
// DonateHub Frontend Core Application Logic & State Controller
// ===================================================================

const API_BASE = 'http://localhost:8080/api';

// In-Memory Application State
let state = {
  drives: [],
  items: [],
  undistributedItems: [],
  distributions: [],
  donors: [],
  recipients: [],
  summary: null,
  activeTab: 'tab-dashboard',
  selectedCategory: 'ALL'
};

// Category Metadata mapping with icons & CSS classes
const CATEGORY_META = {
  CLOTHING: { label: 'Clothing', icon: '👕', class: 'cat-clothing' },
  BEDDING: { label: 'Bedding', icon: '🛏️', class: 'cat-bedding' },
  FOOD: { label: 'Food', icon: '🍞', class: 'cat-food' },
  BOOKS: { label: 'Books', icon: '📚', class: 'cat-books' },
  TOYS: { label: 'Toys', icon: '🧸', class: 'cat-toys' },
  MEDICAL_SUPPLIES: { label: 'Medical', icon: '🩺', class: 'cat-medical' },
  HYGIENE_PRODUCTS: { label: 'Hygiene', icon: '🧼', class: 'cat-hygiene' },
  ELECTRONICS: { label: 'Electronics', icon: '💻', class: 'cat-electronics' },
  OTHER: { label: 'Other', icon: '📦', class: 'cat-other' }
};

// Initial Setup
document.addEventListener('DOMContentLoaded', () => {
  setupTabs();
  setupModalBackdropListeners();
  setDefaultDates();
  loadAllData();
});

// Setup Navigation Tabs
function setupTabs() {
  const tabs = document.querySelectorAll('.nav-tab');
  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      tabs.forEach(t => t.classList.remove('active'));
      document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));

      tab.classList.add('active');
      const targetId = tab.getAttribute('data-tab');
      const targetContent = document.getElementById(targetId);
      if (targetContent) targetContent.classList.add('active');
      state.activeTab = targetId;
    });
  });
}

// Modal Backdrop Click & Escape Key Listeners
function setupModalBackdropListeners() {
  document.querySelectorAll('.modal-backdrop').forEach(modal => {
    modal.addEventListener('click', (e) => {
      if (e.target === modal) {
        closeModal(modal.id);
      }
    });
  });

  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      document.querySelectorAll('.modal-backdrop.open').forEach(m => closeModal(m.id));
    }
  });
}

// Set Today's & Default Dates
function setDefaultDates() {
  const today = new Date().toISOString().split('T')[0];
  const itemDate = document.getElementById('itemReceivedDate');
  const distDate = document.getElementById('distDate');
  const driveStart = document.getElementById('driveStartDate');
  const driveEnd = document.getElementById('driveEndDate');

  if (itemDate) itemDate.value = today;
  if (distDate) distDate.value = today;
  if (driveStart) driveStart.value = today;
  if (driveEnd) {
    const nextMonth = new Date();
    nextMonth.setDate(nextMonth.getDate() + 30);
    driveEnd.value = nextMonth.toISOString().split('T')[0];
  }
}

// Master Data Loader
async function loadAllData() {
  try {
    await Promise.all([
      fetchDrives(),
      fetchDonors(),
      fetchRecipients(),
      fetchItems(),
      fetchUndistributedItems(),
      fetchDistributions(),
      fetchOverallSummary()
    ]);
    updateConnectionStatus(true);
    renderDashboard();
    renderDrivesTab();
    renderItemsTab();
    renderDistributionsTab();
    renderStakeholdersTab();
    populateSelectDropdowns();
  } catch (err) {
    console.error('API connection error:', err);
    updateConnectionStatus(false);
  }
}

function updateConnectionStatus(connected) {
  const badge = document.getElementById('apiStatusBadge');
  const text = document.getElementById('apiStatusText');
  if (!badge || !text) return;

  if (connected) {
    badge.className = 'api-status connected';
    text.textContent = 'Backend Online (MySQL 9)';
  } else {
    badge.className = 'api-status disconnected';
    text.textContent = 'Backend Offline';
  }
}

// ================= API FETCH CALLS =================

async function fetchDrives() {
  const res = await fetch(`${API_BASE}/drives`);
  if (!res.ok) throw new Error('Failed to load drives');
  state.drives = await res.json();
}

async function fetchDonors() {
  const res = await fetch(`${API_BASE}/donors`);
  if (!res.ok) throw new Error('Failed to load donors');
  state.donors = await res.json();
}

async function fetchRecipients() {
  const res = await fetch(`${API_BASE}/recipients`);
  if (!res.ok) throw new Error('Failed to load recipients');
  state.recipients = await res.json();
}

async function fetchItems() {
  const res = await fetch(`${API_BASE}/items`);
  if (!res.ok) throw new Error('Failed to load items');
  state.items = await res.json();
}

async function fetchUndistributedItems() {
  const res = await fetch(`${API_BASE}/items/undistributed`);
  if (!res.ok) throw new Error('Failed to load undistributed items');
  state.undistributedItems = await res.json();
}

async function fetchDistributions() {
  const res = await fetch(`${API_BASE}/distributions`);
  if (!res.ok) throw new Error('Failed to load distributions');
  state.distributions = await res.json();
}

async function fetchOverallSummary() {
  const res = await fetch(`${API_BASE}/items/summary`);
  if (!res.ok) throw new Error('Failed to load summary');
  state.summary = await res.json();
}

// ================= RENDER METHODS =================

function renderDashboard() {
  // KPI Metrics
  document.getElementById('kpiDrivesCount').textContent = state.drives.length;
  if (state.summary) {
    animateCount('kpiItemsCollected', state.summary.totalUnitsCollected || 0);
    animateCount('kpiItemsDistributed', state.summary.totalUnitsDistributed || 0);
    animateCount('kpiRemainingStock', state.summary.totalRemainingStock || 0);
  }

  loadDriveSummariesTable();
  renderRemainingStockTable();
}

function animateCount(elementId, targetValue) {
  const el = document.getElementById(elementId);
  if (!el) return;
  el.textContent = targetValue;
}

// Drive Performance & Summaries
async function loadDriveSummariesTable() {
  const tbody = document.getElementById('driveSummaryTableBody');
  if (!state.drives.length) {
    tbody.innerHTML = `
      <tr>
        <td colspan="8" class="text-center py-8">
          <div style="font-size:2.5rem; margin-bottom:0.5rem;">🎯</div>
          <div style="font-weight:700; color:var(--text-main); font-size:1.05rem;">No Donation Drives Registered</div>
          <div class="text-muted" style="font-size:0.85rem; margin-top:0.25rem;">Start your first relief initiative by clicking "+ New Campaign" above.</div>
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted py-8">Calculating real-time drive summaries...</td></tr>`;

  try {
    const summaryPromises = state.drives.map(d =>
      fetch(`${API_BASE}/drives/${d.id}/summary`).then(r => r.json())
    );
    const summaries = await Promise.all(summaryPromises);

    tbody.innerHTML = summaries.map(s => {
      const pct = s.distributionPercentage || 0;
      return `
        <tr>
          <td>
            <div style="font-weight:700; color:var(--text-main); font-size:0.95rem;">${escapeHtml(s.driveName)}</div>
          </td>
          <td>
            <span class="text-muted" style="font-size:0.825rem;">📅 ${s.startDate} &rarr; ${s.endDate}</span>
          </td>
          <td>
            <span class="badge ${s.status === 'ACTIVE' ? 'badge-success' : 'badge-muted'}">${s.status}</span>
          </td>
          <td><strong>${s.totalItemsCollected}</strong> units</td>
          <td><span style="color:var(--primary); font-weight:700;">${s.totalItemsDistributed}</span> units</td>
          <td><span style="color:var(--success); font-weight:700;">${s.remainingStock}</span> units</td>
          <td>
            <div class="progress-bar-container">
              <div class="progress-bar-fill" style="width: ${Math.min(pct, 100)}%;"></div>
            </div>
            <strong style="font-size:0.825rem;">${pct}%</strong>
          </td>
          <td>
            <button class="btn btn-outline btn-sm" onclick="viewDriveSummaryModal(${s.driveId})">Analytics</button>
          </td>
        </tr>
      `;
    }).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted py-8">Could not load per-drive summaries.</td></tr>`;
  }
}

// Remaining Stock Table
function renderRemainingStockTable() {
  const tbody = document.getElementById('remainingStockTableBody');
  const badge = document.getElementById('undistributedCountBadge');
  if (badge) badge.textContent = `${state.undistributedItems.length} In-Stock Items`;

  if (!state.undistributedItems.length) {
    tbody.innerHTML = `
      <tr>
        <td colspan="8" class="text-center py-8">
          <div style="font-size:2.5rem; margin-bottom:0.5rem;">🎉</div>
          <div style="font-weight:700; color:var(--text-main);">Zero Stock Pending</div>
          <div class="text-muted" style="font-size:0.85rem;">All received items have been successfully dispatched to recipient organizations.</div>
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = state.undistributedItems.map(item => {
    const driveName = item.drive ? item.drive.name : 'N/A';
    const donorName = item.donor ? item.donor.name : 'Anonymous';
    const cat = CATEGORY_META[item.category] || { label: item.category, icon: '📦', class: '' };

    return `
      <tr>
        <td>
          <div style="font-weight:700; color:var(--text-main);">${escapeHtml(item.itemName)}</div>
          ${item.notes ? `<div style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(item.notes)}</div>` : ''}
        </td>
        <td>
          <span class="cat-pill ${cat.class}">${cat.icon} ${cat.label}</span>
        </td>
        <td><span style="font-weight:600;">${escapeHtml(driveName)}</span></td>
        <td>${escapeHtml(donorName)}</td>
        <td><span class="badge badge-muted">${item.itemCondition}</span></td>
        <td>
          <span style="color:var(--success); font-weight:800; font-size:0.95rem;">${item.availableQuantity}</span>
          <span class="text-muted"> / ${item.totalQuantity} units</span>
        </td>
        <td><span class="badge badge-success">${item.status}</span></td>
        <td>
          <button class="btn btn-success btn-sm shadow-glow-green" onclick="quickDistribute(${item.id})">
            ⚡ Dispatch
          </button>
        </td>
      </tr>
    `;
  }).join('');
}

// Drives Grid Tab
function renderDrivesTab() {
  const container = document.getElementById('drivesGrid');
  if (!state.drives.length) {
    container.innerHTML = `
      <div class="card" style="padding:3rem 2rem; text-align:center; grid-column:1/-1;">
        <div style="font-size:3rem; margin-bottom:0.5rem;">🚀</div>
        <h3 style="font-size:1.25rem; font-weight:800; margin-bottom:0.35rem;">No Active Campaigns</h3>
        <p class="text-muted" style="margin-bottom:1.5rem;">Launch your first donation drive to start accepting and cataloging resources.</p>
        <button class="btn btn-primary" onclick="openModal('modalCreateDrive')">+ Create Donation Drive</button>
      </div>
    `;
    return;
  }

  container.innerHTML = state.drives.map(d => `
    <div class="drive-card">
      <div>
        <div class="drive-card-top">
          <h3 class="drive-card-title">${escapeHtml(d.name)}</h3>
          <span class="badge ${d.status === 'ACTIVE' ? 'badge-success' : 'badge-muted'}">${d.status}</span>
        </div>
        <div class="drive-card-timeline mt-4">📅 ${d.startDate} &rarr; ${d.endDate}</div>
        ${d.location ? `<div class="drive-card-location mt-4">📍 ${escapeHtml(d.location)}</div>` : ''}
        ${d.description ? `<p class="drive-card-desc mt-4">${escapeHtml(d.description)}</p>` : ''}
      </div>

      <div class="drive-card-footer">
        <button class="btn btn-outline btn-sm" onclick="viewDriveSummaryModal(${d.id})">📊 Metrics</button>
        <button class="btn btn-primary btn-sm" onclick="quickAddItemForDrive(${d.id})">+ Log Item</button>
      </div>
    </div>
  `).join('');
}

// Filter Drives Cards via Search
function filterDrivesCards() {
  const query = (document.getElementById('searchDrivesInput')?.value || '').toLowerCase();
  const cards = document.querySelectorAll('#drivesGrid .drive-card');
  cards.forEach(card => {
    const text = card.textContent.toLowerCase();
    card.style.display = text.includes(query) ? 'flex' : 'none';
  });
}

// Donated Items Inventory Tab
function renderItemsTab() {
  const tbody = document.getElementById('allItemsTableBody');
  let filtered = state.items;

  if (state.selectedCategory && state.selectedCategory !== 'ALL') {
    filtered = filtered.filter(i => i.category === state.selectedCategory);
  }

  if (!filtered.length) {
    tbody.innerHTML = `<tr><td colspan="11" class="text-center text-muted py-8">No matching items found.</td></tr>`;
    return;
  }

  tbody.innerHTML = filtered.map(i => {
    const driveName = i.drive ? i.drive.name : 'N/A';
    const donorName = i.donor ? i.donor.name : 'N/A';
    const cat = CATEGORY_META[i.category] || { label: i.category, icon: '📦', class: '' };

    return `
      <tr>
        <td><span style="font-family:var(--font-mono); font-size:0.8rem;">#${i.id}</span></td>
        <td>
          <div style="font-weight:700; color:var(--text-main);">${escapeHtml(i.itemName)}</div>
          ${i.notes ? `<div style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(i.notes)}</div>` : ''}
        </td>
        <td><span class="cat-pill ${cat.class}">${cat.icon} ${cat.label}</span></td>
        <td><span class="badge badge-muted">${i.itemCondition}</span></td>
        <td><strong>${escapeHtml(driveName)}</strong></td>
        <td>${escapeHtml(donorName)}</td>
        <td><strong>${i.totalQuantity}</strong></td>
        <td><span style="color:var(--success); font-weight:800;">${i.availableQuantity}</span></td>
        <td><span class="text-muted" style="font-size:0.8rem;">${i.receivedDate || '-'}</span></td>
        <td>
          <span class="badge ${i.status === 'AVAILABLE' ? 'badge-success' : (i.status === 'DISTRIBUTED' ? 'badge-muted' : 'badge-warning')}">
            ${i.status}
          </span>
        </td>
        <td>
          ${i.availableQuantity > 0 ? `
            <button class="btn btn-outline btn-sm" onclick="quickDistribute(${i.id})">Dispatch</button>
          ` : `<span class="text-muted" style="font-size:0.75rem;">Fulfilled</span>`}
        </td>
      </tr>
    `;
  }).join('');
}

// Category Filter Chips
function filterItemsByCategory(category) {
  state.selectedCategory = category;
  document.querySelectorAll('#categoryChipsBar .chip').forEach(c => c.classList.remove('active'));
  event?.target?.classList.add('active');
  renderItemsTab();
}

// Search Filter for Items
function filterItemsTable() {
  const query = (document.getElementById('searchItemsInput')?.value || '').toLowerCase();
  const rows = document.querySelectorAll('#allItemsTableBody tr');
  rows.forEach(r => {
    const text = r.textContent.toLowerCase();
    r.style.display = text.includes(query) ? '' : 'none';
  });
}

// Distributions Audit Tab
function renderDistributionsTab() {
  const tbody = document.getElementById('distributionsTableBody');
  if (!state.distributions.length) {
    tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted py-8">No distributions recorded yet.</td></tr>`;
    return;
  }

  tbody.innerHTML = state.distributions.map(d => {
    const itemName = d.donatedItem ? d.donatedItem.itemName : 'N/A';
    const recipientName = d.recipient ? d.recipient.organizationName : 'N/A';
    const driveName = d.drive ? d.drive.name : 'N/A';

    return `
      <tr>
        <td><span style="font-family:var(--font-mono); font-size:0.8rem;">#DIST-${d.id}</span></td>
        <td><span class="text-muted" style="font-size:0.825rem;">📅 ${d.distributionDate}</span></td>
        <td><strong>${escapeHtml(itemName)}</strong></td>
        <td><span style="color:var(--primary); font-weight:700;">${escapeHtml(recipientName)}</span></td>
        <td><strong style="color:var(--success); font-size:0.95rem;">${d.distributedQuantity}</strong> units</td>
        <td>${escapeHtml(driveName)}</td>
        <td><span style="font-size:0.825rem;">${escapeHtml(d.purpose || '-')}</span></td>
        <td><span class="badge badge-purple">${escapeHtml(d.distributedBy || 'Volunteer')}</span></td>
      </tr>
    `;
  }).join('');
}

// Filter Distributions
function filterDistributionsTable() {
  const query = (document.getElementById('searchDistributionsInput')?.value || '').toLowerCase();
  const rows = document.querySelectorAll('#distributionsTableBody tr');
  rows.forEach(r => {
    const text = r.textContent.toLowerCase();
    r.style.display = text.includes(query) ? '' : 'none';
  });
}

// Stakeholders (Donors & Recipients) Tab
function renderStakeholdersTab() {
  // Donors
  const dBody = document.getElementById('donorsTableBody');
  dBody.innerHTML = state.donors.length ? state.donors.map(d => `
    <tr>
      <td>
        <div style="font-weight:700; color:var(--text-main);">${escapeHtml(d.name)}</div>
      </td>
      <td>
        <div>${escapeHtml(d.email || '-')}</div>
        <div class="text-muted" style="font-size:0.75rem;">${escapeHtml(d.phoneNumber || '')}</div>
      </td>
      <td><span class="text-muted" style="font-size:0.825rem;">${escapeHtml(d.address || '-')}</span></td>
    </tr>
  `).join('') : `<tr><td colspan="3" class="text-center text-muted py-8">No donors registered yet.</td></tr>`;

  // Recipients
  const rBody = document.getElementById('recipientsTableBody');
  rBody.innerHTML = state.recipients.length ? state.recipients.map(r => `
    <tr>
      <td>
        <div style="font-weight:700; color:var(--text-main);">${escapeHtml(r.organizationName)}</div>
        <div class="text-muted" style="font-size:0.75rem;">${escapeHtml(r.address || '')}</div>
      </td>
      <td><strong>${escapeHtml(r.contactPerson || '-')}</strong></td>
      <td>
        <div>${escapeHtml(r.phoneNumber || '-')}</div>
        <div class="text-muted" style="font-size:0.75rem;">${escapeHtml(r.contactEmail || '')}</div>
      </td>
    </tr>
  `).join('') : `<tr><td colspan="3" class="text-center text-muted py-8">No recipients registered yet.</td></tr>`;
}

// Dropdown Populators
function populateSelectDropdowns() {
  const driveSelect = document.getElementById('itemDriveSelect');
  const filterSelect = document.getElementById('filterDriveSelect');
  const driveOptions = state.drives.map(d => `<option value="${d.id}">${escapeHtml(d.name)}</option>`).join('');

  if (driveSelect) driveSelect.innerHTML = `<option value="">Select an active drive...</option>` + driveOptions;
  if (filterSelect) filterSelect.innerHTML = `<option value="">All Donation Drives</option>` + driveOptions;

  const donorSelect = document.getElementById('itemDonorSelect');
  if (donorSelect) {
    donorSelect.innerHTML = `<option value="">Select a donor...</option>` +
      state.donors.map(d => `<option value="${d.id}">${escapeHtml(d.name)} (${d.email || d.phoneNumber || 'Donor'})</option>`).join('');
  }

  const recSelect = document.getElementById('distRecipientSelect');
  if (recSelect) {
    recSelect.innerHTML = `<option value="">Select recipient organization...</option>` +
      state.recipients.map(r => `<option value="${r.id}">${escapeHtml(r.organizationName)}</option>`).join('');
  }

  const itemSelect = document.getElementById('distItemSelect');
  if (itemSelect) {
    itemSelect.innerHTML = `<option value="">Select available item...</option>` +
      state.undistributedItems.map(i => `<option value="${i.id}" data-stock="${i.availableQuantity}">${escapeHtml(i.itemName)} (${i.availableQuantity} units left - ${i.drive?.name || 'Drive'})</option>`).join('');
  }
}

// Stock Item Change Handler
function handleStockItemChange() {
  const select = document.getElementById('distItemSelect');
  const qtyInput = document.getElementById('distQuantity');
  const hint = document.getElementById('stockAvailableHint');
  const selected = select.options[select.selectedIndex];

  if (selected && selected.dataset.stock) {
    const stock = parseInt(selected.dataset.stock, 10);
    qtyInput.max = stock;
    qtyInput.placeholder = `Max ${stock} units`;
    hint.textContent = `✓ Available in inventory: ${stock} units`;
    hint.style.color = 'var(--success)';
    hint.style.borderColor = 'var(--success)';
  } else {
    qtyInput.removeAttribute('max');
    qtyInput.placeholder = 'Units';
    hint.textContent = 'Select an item above to inspect available balance.';
    hint.style.color = 'var(--text-muted)';
    hint.style.borderColor = 'var(--border)';
  }
}

// ================= FORM HANDLERS =================

async function handleCreateDrive(e) {
  e.preventDefault();
  const payload = {
    name: document.getElementById('driveName').value.trim(),
    startDate: document.getElementById('driveStartDate').value,
    endDate: document.getElementById('driveEndDate').value,
    location: document.getElementById('driveLocation').value.trim(),
    description: document.getElementById('driveDescription').value.trim(),
    status: 'ACTIVE'
  };

  try {
    const res = await fetch(`${API_BASE}/drives`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Failed to create drive');
    }
    showToast('Donation campaign launched successfully!', 'success');
    closeModal('modalCreateDrive');
    e.target.reset();
    setDefaultDates();
    await loadAllData();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function handleLogItem(e) {
  e.preventDefault();
  const driveId = document.getElementById('itemDriveSelect').value;
  const donorId = document.getElementById('itemDonorSelect').value;

  const payload = {
    itemName: document.getElementById('itemName').value.trim(),
    category: document.getElementById('itemCategory').value,
    itemCondition: document.getElementById('itemCondition').value,
    totalQuantity: parseInt(document.getElementById('itemQuantity').value, 10),
    receivedDate: document.getElementById('itemReceivedDate').value,
    notes: document.getElementById('itemNotes').value.trim()
  };

  try {
    const res = await fetch(`${API_BASE}/items?driveId=${driveId}&donorId=${donorId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Failed to log donated item');
    }
    showToast('Item recorded into inventory!', 'success');
    closeModal('modalLogItem');
    e.target.reset();
    setDefaultDates();
    await loadAllData();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function handleRecordDistribution(e) {
  e.preventDefault();
  const itemId = document.getElementById('distItemSelect').value;
  const recipientId = document.getElementById('distRecipientSelect').value;
  const quantity = parseInt(document.getElementById('distQuantity').value, 10);
  const distributionDate = document.getElementById('distDate').value;
  const purpose = document.getElementById('distPurpose').value.trim();
  const distributedBy = document.getElementById('distBy').value.trim();

  const payload = {
    itemId: parseInt(itemId, 10),
    recipientId: parseInt(recipientId, 10),
    quantity,
    distributionDate,
    purpose,
    distributedBy
  };

  try {
    const res = await fetch(`${API_BASE}/distributions`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Failed to record distribution');
    }
    showToast('Stock dispatched & audit record updated!', 'success');
    closeModal('modalDistribute');
    e.target.reset();
    setDefaultDates();
    await loadAllData();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function handleCreateDonor(e) {
  e.preventDefault();
  const payload = {
    name: document.getElementById('donorName').value.trim(),
    email: document.getElementById('donorEmail').value.trim() || null,
    phoneNumber: document.getElementById('donorPhone').value.trim() || null,
    address: document.getElementById('donorAddress').value.trim() || null
  };

  try {
    const res = await fetch(`${API_BASE}/donors`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Failed to save donor');
    }
    showToast('Donor profile created successfully!', 'success');
    closeModal('modalDonor');
    e.target.reset();
    await loadAllData();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function handleCreateRecipient(e) {
  e.preventDefault();
  const payload = {
    organizationName: document.getElementById('recipientName').value.trim(),
    contactPerson: document.getElementById('recipientContactPerson').value.trim() || null,
    phoneNumber: document.getElementById('recipientPhone').value.trim() || null,
    contactEmail: document.getElementById('recipientEmail').value.trim() || null,
    address: document.getElementById('recipientAddress').value.trim() || null
  };

  try {
    const res = await fetch(`${API_BASE}/recipients`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Failed to save recipient');
    }
    showToast('Recipient organization registered!', 'success');
    closeModal('modalRecipient');
    e.target.reset();
    await loadAllData();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// Quick Actions & Modal Triggers
function quickDistribute(itemId) {
  openModal('modalDistribute');
  const select = document.getElementById('distItemSelect');
  if (select) {
    select.value = itemId;
    handleStockItemChange();
  }
}

function quickAddItemForDrive(driveId) {
  openModal('modalLogItem');
  const select = document.getElementById('itemDriveSelect');
  if (select) select.value = driveId;
}

// Modern Drive Summary Dialog (Replaced native alert)
async function viewDriveSummaryModal(driveId) {
  try {
    const res = await fetch(`${API_BASE}/drives/${driveId}/summary`);
    if (!res.ok) throw new Error('Could not fetch summary');
    const s = await res.json();

    document.getElementById('metricModalDriveName').textContent = s.driveName;
    document.getElementById('metricModalDates').textContent = `Active Period: ${s.startDate} to ${s.endDate} • Status: ${s.status}`;
    document.getElementById('metricModalCollected').textContent = s.totalItemsCollected;
    document.getElementById('metricModalDistributed').textContent = s.totalItemsDistributed;
    document.getElementById('metricModalStock').textContent = s.remainingStock;
    document.getElementById('metricModalPct').textContent = `${s.distributionPercentage}%`;
    document.getElementById('metricModalProgressFill').style.width = `${Math.min(s.distributionPercentage, 100)}%`;

    const actionBtn = document.getElementById('metricModalActionBtn');
    if (actionBtn) {
      actionBtn.onclick = () => {
        closeModal('modalDriveMetrics');
        quickAddItemForDrive(driveId);
      };
    }

    openModal('modalDriveMetrics');
  } catch (err) {
    showToast('Failed to load drive analytics', 'error');
  }
}

async function filterItemsByDrive() {
  const driveId = document.getElementById('filterDriveSelect').value;
  try {
    const url = driveId ? `${API_BASE}/items?driveId=${driveId}` : `${API_BASE}/items`;
    const res = await fetch(url);
    state.items = await res.json();
    renderItemsTab();
  } catch (err) {
    showToast('Filter error', 'error');
  }
}

// Modal Controllers
function openModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.add('open');
}

function closeModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.remove('open');
}

// Toast System
function showToast(msg, type = 'info') {
  const container = document.getElementById('toastContainer');
  if (!container) return;
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `
    <span>${type === 'success' ? '✓' : (type === 'error' ? '✕' : 'ℹ')}</span>
    <span>${msg}</span>
  `;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

// HTML Escaping utility
function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

function loadDashboard() {
  loadAllData();
  showToast('Dashboard metrics refreshed!', 'info');
}
