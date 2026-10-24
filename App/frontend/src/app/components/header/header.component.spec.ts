
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { HeaderComponent } from './header.component';
import { AuthService } from 'src/app/services/auth.service';
import { ToastService } from 'angular-toastify';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;

  const authServiceMock = {
    isLoggedIn: () => true,
    logOutUser: () => of({}),
  };

  const routerMock = {
    navigate: jasmine.createSpy('navigate'),
  };

  const toastServiceMock = {
    error: jasmine.createSpy('error'),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [HeaderComponent],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock },
        { provide: ToastService, useValue: toastServiceMock },
      ],
    })
      .overrideComponent(HeaderComponent, {
        set: { template: '' },
      })
      .compileComponents();

    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should clear the token and navigate to login on logout', () => {
    spyOn(localStorage, 'removeItem');
    component.logout();

    expect(localStorage.removeItem).toHaveBeenCalledWith('authToken');
    expect(routerMock.navigate).toHaveBeenCalledWith(['/login']);
  });
}); 