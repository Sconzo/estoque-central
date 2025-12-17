import { TestBed } from '@angular/core/testing';
import { LOCALE_ID } from '@angular/core';
import { MAT_DATE_LOCALE } from '@angular/material/core';
import { appConfig } from './app.config';

describe('AppConfig', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: appConfig.providers
    });
  });

  it('should provide LOCALE_ID as pt-BR', () => {
    const localeId = TestBed.inject(LOCALE_ID);
    expect(localeId).toBe('pt-BR');
  });

  it('should provide MAT_DATE_LOCALE as pt-BR', () => {
    const matDateLocale = TestBed.inject(MAT_DATE_LOCALE);
    expect(matDateLocale).toBe('pt-BR');
  });
});
