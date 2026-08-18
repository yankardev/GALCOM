import { Component, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell.html',
  styleUrl: './shell.css'
})
export class Shell {
  readonly menuOpen = signal(false);
  readonly compact = signal(false);

  constructor(public readonly auth: AuthService, private readonly router: Router) {}

  toggleMenu(): void { this.menuOpen.update(v => !v); }
  toggleCompact(): void { this.compact.update(v => !v); }
  closeMenu(): void { this.menuOpen.set(false); }
  exit(): void { this.auth.logout(); this.router.navigateByUrl('/login'); }
}
